package com.dori.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.dori.app.data.Label
import com.dori.app.data.NoteRepository
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LabelsViewModel(private val repository: NoteRepository) : ViewModel() {

    val labels: StateFlow<List<Label>> = repository.getAllLabels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createLabel(name: String, colorArgb: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertLabel(Label(name = name.trim(), colorArgb = colorArgb))
        }
    }

    fun updateLabel(label: Label, name: String, colorArgb: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateLabel(label.copy(name = name.trim(), colorArgb = colorArgb))
        }
    }

    fun deleteLabel(label: Label) {
        viewModelScope.launch { repository.deleteLabel(label) }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return LabelsViewModel(repository) as T
        }
    }
}
