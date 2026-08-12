package com.dori.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dori.app.data.Label
import com.dori.app.data.LabelColorPalette
import com.dori.app.data.NoteRepository
import com.dori.app.viewmodel.LabelsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelsPane(repository: NoteRepository) {
    val viewModel = remember { LabelsViewModel(repository) }
    val labels by viewModel.labels.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingLabel by remember { mutableStateOf<Label?>(null) }
    var deletingLabel by remember { mutableStateOf<Label?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Labels") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New label")
            }
        }
    ) { innerPadding ->
        if (labels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No labels yet. Click + to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(labels, key = { it.id }) { label ->
                    ListItem(
                        modifier = Modifier.clickable { editingLabel = label },
                        leadingContent = { LabelDot(label.colorArgb, dotSize = 20.dp) },
                        headlineContent = { Text(label.name) },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editingLabel = label }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit ${label.name}")
                                }
                                IconButton(onClick = { deletingLabel = label }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete ${label.name}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        LabelEditorDialog(
            dialogTitle = "New label",
            initialName = "",
            initialColor = LabelColorPalette.first(),
            onConfirm = { name, color ->
                viewModel.createLabel(name, color)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    editingLabel?.let { label ->
        LabelEditorDialog(
            dialogTitle = "Edit label",
            initialName = label.name,
            initialColor = label.colorArgb,
            onConfirm = { name, color ->
                viewModel.updateLabel(label, name, color)
                editingLabel = null
            },
            onDismiss = { editingLabel = null }
        )
    }

    deletingLabel?.let { label ->
        AlertDialog(
            onDismissRequest = { deletingLabel = null },
            title = { Text("Delete \"${label.name}\"?") },
            text = { Text("Notes using this label will become unlabeled.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLabel(label)
                    deletingLabel = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingLabel = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LabelEditorDialog(
    dialogTitle: String,
    initialName: String,
    initialColor: Int,
    onConfirm: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow {
                    LabelColorPalette.forEach { swatch ->
                        ColorSwatch(
                            colorArgb = swatch,
                            selected = swatch == color,
                            onClick = { color = swatch }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, color) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun ColorSwatch(colorArgb: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(36.dp)
            .clickable(onClick = onClick)
            .background(Color(colorArgb), CircleShape)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
        }
    }
}
