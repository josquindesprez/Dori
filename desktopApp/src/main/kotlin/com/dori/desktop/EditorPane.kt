package com.dori.desktop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dori.app.data.Label
import com.dori.app.data.NoteRepository
import com.dori.app.viewmodel.EditorViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val transparentFieldColors: @Composable () -> androidx.compose.material3.TextFieldColors = {
    TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorPane(
    repository: NoteRepository,
    noteId: Long?,
    initialDiaryDate: LocalDate?,
    onBack: () -> Unit
) {
    val viewModel = remember(noteId, initialDiaryDate) {
        EditorViewModel(repository, noteId, initialDiaryDate)
    }
    // Desktop has no NavBackStackEntry-scoped ViewModelStore to call
    // onCleared() automatically, so flush explicitly when this pane leaves
    // composition (see EditorViewModel.flushOnExit).
    DisposableEffect(viewModel) {
        onDispose { viewModel.flushOnExit() }
    }

    val uiState by viewModel.uiState.collectAsState()
    val labels by viewModel.labels.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showLabelPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!uiState.isLoading) {
                        IconButton(onClick = viewModel::delete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete note")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            TextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Title") },
                textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
                singleLine = true,
                colors = transparentFieldColors()
            )
            TextField(
                value = uiState.body,
                onValueChange = viewModel::onBodyChange,
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Write...") },
                textStyle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
                colors = transparentFieldColors()
            )

            DiarySection(
                diaryDate = uiState.diaryDate,
                onToggle = { inDiary ->
                    viewModel.setDiaryDate(if (inDiary) (uiState.diaryDate ?: LocalDate.now()) else null)
                },
                onPickDate = { showDatePicker = true }
            )

            LabelSection(
                currentLabel = labels.find { it.id == uiState.labelId },
                onClick = { showLabelPicker = true }
            )
        }
    }

    if (showDatePicker) {
        val initialMillis = (uiState.diaryDate ?: LocalDate.now())
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.setDiaryDate(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate())
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLabelPicker) {
        AlertDialog(
            onDismissRequest = { showLabelPicker = false },
            title = { Text("Label") },
            text = {
                Column(modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                    ListItem(
                        modifier = Modifier.clickable {
                            viewModel.setLabel(null)
                            showLabelPicker = false
                        },
                        headlineContent = { Text("No label") }
                    )
                    labels.forEach { label ->
                        ListItem(
                            modifier = Modifier.clickable {
                                viewModel.setLabel(label.id)
                                showLabelPicker = false
                            },
                            leadingContent = { LabelDot(label.colorArgb, dotSize = 16.dp) },
                            headlineContent = { Text(label.name) }
                        )
                    }
                    if (labels.isEmpty()) {
                        Text(
                            "No labels yet. Create one from the Labels tab.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showLabelPicker = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun DiarySection(diaryDate: LocalDate?, onToggle: (Boolean) -> Unit, onPickDate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(checked = diaryDate != null, onCheckedChange = onToggle)
        Text(
            text = if (diaryDate != null) "In Diary" else "Loose note",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
        if (diaryDate != null) {
            TextButton(onClick = onPickDate) { Text(formatDateChip(diaryDate)) }
        }
    }
}

@Composable
private fun LabelSection(currentLabel: Label?, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Sell, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(6.dp))
        if (currentLabel != null) {
            LabelChip(currentLabel)
        } else {
            Text(
                "No label",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
