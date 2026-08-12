package com.dori.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dori.app.data.Label
import com.dori.app.data.Note
import com.dori.app.data.NoteRepository
import com.dori.app.viewmodel.DiaryViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryPane(
    repository: NoteRepository,
    onNoteClick: (Long) -> Unit,
    onCreateNoteForDate: (LocalDate) -> Unit
) {
    val viewModel = remember { DiaryViewModel(repository) }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val notes by viewModel.notesForSelectedDate.collectAsState()
    val labels by viewModel.labels.collectAsState()
    val labelsById = labels.associateBy { it.id }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Diary") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateNoteForDate(selectedDate) }) {
                Icon(Icons.Filled.Add, contentDescription = "New diary note")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.selectDate(selectedDate.minusDays(1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day")
                }
                TextButton(onClick = { showDatePicker = true }) {
                    Text(formatDateChip(selectedDate), style = MaterialTheme.typography.titleMedium)
                }
                Row {
                    IconButton(onClick = { viewModel.selectDate(LocalDate.now()) }) {
                        Icon(Icons.Filled.Today, contentDescription = "Jump to today")
                    }
                    IconButton(onClick = { viewModel.selectDate(selectedDate.plusDays(1)) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next day")
                    }
                }
            }

            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No notes for this day yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notes, key = { it.id }) { note ->
                        DesktopDiaryNoteRow(
                            note = note,
                            label = note.labelId?.let { labelsById[it] },
                            onClick = { onNoteClick(note.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.selectDate(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DesktopDiaryNoteRow(note: Note, label: Label?, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ListItem(
            modifier = Modifier.padding(horizontal = 4.dp),
            headlineContent = {
                Text(text = note.title.ifBlank { "Untitled" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = if (note.body.isNotBlank() || label != null) {
                {
                    Column {
                        if (note.body.isNotBlank()) {
                            Text(text = bodyPreview(note.body), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        if (label != null) {
                            LabelChip(label, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            } else null
        )
    }
}
