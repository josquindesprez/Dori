package com.dori.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.dori.app.data.AppScope
import com.dori.app.data.Label
import com.dori.app.data.Note
import com.dori.app.data.NoteRepository
import com.dori.app.data.OpenEditorRegistry
import com.dori.app.data.PendingShare
import kotlin.reflect.KClass
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

data class EditorUiState(
    val id: Long? = null,
    // The note's stable cross-device identity. Generated once, either here
    // (new note) or loaded from the existing row, and never regenerated -
    // every persist of this editing session reuses the same uuid.
    val uuid: String = "",
    val title: String = "",
    val body: String = "",
    val diaryDate: LocalDate? = null,
    val labelId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false
) {
    val isEmptyContent: Boolean
        get() = title.isBlank() && body.isBlank()
}

private const val AUTOSAVE_DEBOUNCE_MS = 400L

class EditorViewModel(
    private val repository: NoteRepository,
    noteId: Long?,
    initialDiaryDate: LocalDate?
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EditorUiState(
            diaryDate = initialDiaryDate,
            isLoading = noteId != null,
            uuid = if (noteId == null) UUID.randomUUID().toString() else "",
            // A brand new editor is the only time it's safe to claim a pending
            // share - reusing an existing note must never splice foreign text
            // into it.
            body = if (noteId == null) PendingShare.consume().orEmpty() else ""
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    val labels: StateFlow<List<Label>> = repository.getAllLabels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var saveJob: Job? = null

    init {
        if (noteId != null) {
            viewModelScope.launch {
                val existing = repository.getNoteById(noteId).first()
                _uiState.value = if (existing != null) {
                    OpenEditorRegistry.register(existing.id)
                    EditorUiState(
                        id = existing.id,
                        uuid = existing.uuid,
                        title = existing.title,
                        body = existing.body,
                        diaryDate = existing.diaryDate,
                        labelId = existing.labelId,
                        createdAt = existing.createdAt,
                        isLoading = false
                    )
                } else {
                    _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
        scheduleAutosave()
    }

    fun onBodyChange(value: String) {
        _uiState.update { it.copy(body = value) }
        scheduleAutosave()
    }

    fun setDiaryDate(date: LocalDate?) {
        _uiState.update { it.copy(diaryDate = date) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch { persist() }
    }

    fun setLabel(labelId: Long?) {
        _uiState.update { it.copy(labelId = labelId) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch { persist() }
    }

    fun delete() {
        saveJob?.cancel()
        val id = _uiState.value.id
        _uiState.update { it.copy(isDeleted = true) }
        if (id != null) {
            OpenEditorRegistry.unregister(id)
            // Soft delete, not a hard DELETE: this note may already have
            // synced to another device, and the tombstone is what tells that
            // peer to remove its copy too instead of it silently resurrecting.
            AppScope.io.launch { repository.softDeleteNoteById(id) }
        }
    }

    private fun scheduleAutosave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(AUTOSAVE_DEBOUNCE_MS)
            persist()
        }
    }

    private fun labelUuidFor(labelId: Long?): String? =
        labelId?.let { id -> labels.value.find { it.id == id }?.uuid }

    private fun toNote(state: EditorUiState): Note = Note(
        id = state.id ?: 0,
        uuid = state.uuid,
        title = state.title,
        body = state.body,
        diaryDate = state.diaryDate,
        labelId = state.labelId,
        labelUuid = labelUuidFor(state.labelId),
        createdAt = state.createdAt,
        updatedAt = System.currentTimeMillis()
    )

    private suspend fun persist() {
        val state = _uiState.value
        if (state.isEmptyContent) return
        val id = state.id
        if (id == null) {
            val newId = repository.insert(toNote(state))
            // Recording the generated id must survive cancellation of this job
            // (e.g. the user navigates away right as this insert completes) -
            // otherwise the id is lost in memory even though the row now
            // exists, and a later flush would insert a duplicate row.
            withContext(NonCancellable) {
                OpenEditorRegistry.register(newId)
                _uiState.update { it.copy(id = newId) }
            }
        } else {
            repository.update(toNote(state))
        }
    }

    override fun onCleared() {
        scheduleFinalFlush()
    }

    /**
     * Android's NavBackStackEntry-scoped ViewModelStore calls onCleared()
     * automatically when the editor destination is popped. Desktop has no
     * such store - its UI must call this explicitly (e.g. from a
     * DisposableEffect) when the editor leaves composition.
     */
    fun flushOnExit() {
        scheduleFinalFlush()
    }

    private fun scheduleFinalFlush() {
        val pendingJob = saveJob
        pendingJob?.cancel()

        // viewModelScope may already be (or be about to be) cancelled by the
        // caller's lifecycle, so the final flush runs on AppScope, which
        // outlives this ViewModel. It first waits for any in-flight autosave
        // to fully finish (including the NonCancellable id write above) so
        // it reads truly current state instead of racing an insert that
        // hasn't recorded its id yet.
        AppScope.io.launch {
            pendingJob?.join()
            val state = _uiState.value
            state.id?.let { OpenEditorRegistry.unregister(it) }
            if (state.isLoading || state.isDeleted) return@launch

            if (state.isEmptyContent) {
                state.id?.let { repository.deleteById(it) }
            } else if (state.id == null) {
                repository.insert(toNote(state))
            } else {
                repository.update(toNote(state))
            }
        }
    }

    class Factory(
        private val repository: NoteRepository,
        private val noteId: Long?,
        private val initialDiaryDate: LocalDate?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return EditorViewModel(repository, noteId, initialDiaryDate) as T
        }
    }
}
