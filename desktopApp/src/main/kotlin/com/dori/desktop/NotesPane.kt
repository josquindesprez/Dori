package com.dori.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dori.app.data.Label
import com.dori.app.data.Note
import com.dori.app.data.NoteRepository
import com.dori.app.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesPane(
    repository: NoteRepository,
    onNoteClick: (Long) -> Unit,
    onCreateNote: () -> Unit
) {
    val viewModel = remember { NotesViewModel(repository) }
    val notes by viewModel.looseNotes.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val labelsById = labels.associateBy { it.id }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Notes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Icon(Icons.Filled.Add, contentDescription = "New note")
            }
        }
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No notes yet. Click + to write one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(notes, key = { it.id }) { note ->
                    DesktopNoteRow(
                        note = note,
                        label = note.labelId?.let { labelsById[it] },
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopNoteRow(note: Note, label: Label?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ListItem(
            modifier = Modifier.padding(horizontal = 4.dp),
            headlineContent = {
                Text(
                    text = note.title.ifBlank { "Untitled" },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    if (note.body.isNotBlank()) {
                        Text(
                            text = bodyPreview(note.body),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatTimestamp(note.updatedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (label != null) {
                        LabelChip(label, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        )
    }
}
