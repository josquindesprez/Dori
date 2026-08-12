package com.dori.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dori.app.data.LabelColorPalette
import com.dori.app.data.Substance
import com.dori.app.viewmodel.SubstancesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstancesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SubstancesViewModel = viewModel(
        factory = SubstancesViewModel.Factory(context.doriRepository())
    )
    val substances by viewModel.substances.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingSubstance by remember { mutableStateOf<Substance?>(null) }
    var deletingSubstance by remember { mutableStateOf<Substance?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Substances") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New substance")
            }
        }
    ) { innerPadding ->
        if (substances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No substances yet. Tap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(substances, key = { it.id }) { substance ->
                    ListItem(
                        modifier = Modifier.clickable { editingSubstance = substance },
                        leadingContent = { LabelDot(substance.colorArgb, dotSize = 20.dp) },
                        headlineContent = { Text(substance.name) },
                        supportingContent = if (substance.unit.isNotBlank()) {
                            { Text("Unit: ${substance.unit}") }
                        } else null,
                        trailingContent = {
                            Row {
                                IconButton(onClick = { editingSubstance = substance }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit ${substance.name}")
                                }
                                IconButton(onClick = { deletingSubstance = substance }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete ${substance.name}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        SubstanceEditorDialog(
            dialogTitle = "New substance",
            initialName = "",
            initialUnit = "",
            initialColor = LabelColorPalette.first(),
            onConfirm = { name, unit, color ->
                viewModel.createSubstance(name, unit, color)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    editingSubstance?.let { substance ->
        SubstanceEditorDialog(
            dialogTitle = "Edit substance",
            initialName = substance.name,
            initialUnit = substance.unit,
            initialColor = substance.colorArgb,
            onConfirm = { name, unit, color ->
                viewModel.updateSubstance(substance, name, unit, color)
                editingSubstance = null
            },
            onDismiss = { editingSubstance = null }
        )
    }

    deletingSubstance?.let { substance ->
        AlertDialog(
            onDismissRequest = { deletingSubstance = null },
            title = { Text("Delete \"${substance.name}\"?") },
            text = { Text("All logged entries for this substance will be deleted too.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSubstance(substance)
                    deletingSubstance = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingSubstance = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun SubstanceEditorDialog(
    dialogTitle: String,
    initialName: String,
    initialUnit: String,
    initialColor: Int,
    onConfirm: (String, String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var unit by remember { mutableStateOf(initialUnit) }
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g. g, ml, pills)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow {
                    LabelColorPalette.forEach { swatch ->
                        SubstanceColorSwatch(
                            colorArgb = swatch,
                            selected = swatch == color,
                            onClick = { color = swatch }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, unit, color) },
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SubstanceColorSwatch(colorArgb: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(36.dp)
            .clickable(onClick = onClick)
            .background(Color(colorArgb), CircleShape)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
