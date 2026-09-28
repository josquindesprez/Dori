package com.dori.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dori.app.data.NoteRepository
import com.dori.app.sync.SyncEngine
import java.time.LocalDate

sealed interface Pane {
    data object Notes : Pane
    data object Diary : Pane
    data object Tracker : Pane
    data object Labels : Pane
    data object Substances : Pane
    data object Sync : Pane
    data class Editor(val noteId: Long?, val initialDiaryDate: LocalDate?, val returnTo: Pane) : Pane
}

@Composable
fun DoriDesktopApp(repository: NoteRepository, syncEngine: SyncEngine) {
    var currentPane by remember { mutableStateOf<Pane>(Pane.Notes) }

    DoriDesktopTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            if (currentPane !is Pane.Editor) {
                NavigationRail {
                    NavigationRailItem(
                        selected = currentPane is Pane.Notes,
                        onClick = { currentPane = Pane.Notes },
                        icon = { Icon(Icons.Filled.Edit, contentDescription = "Notes") },
                        label = { Text("Notes") }
                    )
                    NavigationRailItem(
                        selected = currentPane is Pane.Diary,
                        onClick = { currentPane = Pane.Diary },
                        icon = { Icon(Icons.Filled.Book, contentDescription = "Diary") },
                        label = { Text("Diary") }
                    )
                    NavigationRailItem(
                        selected = currentPane is Pane.Tracker,
                        onClick = { currentPane = Pane.Tracker },
                        icon = { Icon(Icons.Filled.Medication, contentDescription = "Tracker") },
                        label = { Text("Tracker") }
                    )
                    NavigationRailItem(
                        selected = currentPane is Pane.Labels,
                        onClick = { currentPane = Pane.Labels },
                        icon = { Icon(Icons.Filled.Sell, contentDescription = "Labels") },
                        label = { Text("Labels") }
                    )
                    NavigationRailItem(
                        selected = currentPane is Pane.Substances,
                        onClick = { currentPane = Pane.Substances },
                        icon = { Icon(Icons.Filled.Science, contentDescription = "Substances") },
                        label = { Text("Substances") }
                    )
                    NavigationRailItem(
                        selected = currentPane is Pane.Sync,
                        onClick = { currentPane = Pane.Sync },
                        icon = { Icon(Icons.Filled.Sync, contentDescription = "Sync") },
                        label = { Text("Sync") }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (val pane = currentPane) {
                    is Pane.Notes -> NotesPane(
                        repository = repository,
                        onNoteClick = { id -> currentPane = Pane.Editor(id, null, Pane.Notes) },
                        onCreateNote = { currentPane = Pane.Editor(null, null, Pane.Notes) }
                    )

                    is Pane.Diary -> DiaryPane(
                        repository = repository,
                        onNoteClick = { id -> currentPane = Pane.Editor(id, null, Pane.Diary) },
                        onCreateNoteForDate = { date -> currentPane = Pane.Editor(null, date, Pane.Diary) }
                    )

                    is Pane.Tracker -> TrackerPane(repository = repository)

                    is Pane.Labels -> LabelsPane(repository = repository)

                    is Pane.Substances -> SubstancesPane(repository = repository)

                    is Pane.Sync -> SyncPane(engine = syncEngine)

                    is Pane.Editor -> EditorPane(
                        repository = repository,
                        noteId = pane.noteId,
                        initialDiaryDate = pane.initialDiaryDate,
                        onBack = { currentPane = pane.returnTo }
                    )
                }
            }
        }
    }
}

@Composable
fun DoriDesktopTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
