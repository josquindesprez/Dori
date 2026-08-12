package com.dori.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.dori.app.data.NoteRepository
import com.dori.app.data.Substance
import com.dori.app.data.SubstanceEntry
import com.dori.app.viewmodel.PeriodType
import com.dori.app.viewmodel.SubstanceTotal
import com.dori.app.viewmodel.TrackerViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerPane(repository: NoteRepository) {
    val viewModel = remember { TrackerViewModel(repository) }
    val substances by viewModel.substances.collectAsState()
    val periodType by viewModel.periodType.collectAsState()
    val periodRange by viewModel.periodRange.collectAsState()
    val entries by viewModel.entriesForPeriod.collectAsState()
    val summary by viewModel.periodSummary.collectAsState()
    val substancesById = substances.associateBy { it.id }

    var showEntryDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<SubstanceEntry?>(null) }
    var deletingEntry by remember { mutableStateOf<SubstanceEntry?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tracker") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (substances.isNotEmpty()) {
                    editingEntry = null
                    showEntryDialog = true
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Log entry")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PeriodTypeToggle(periodType, onSelect = viewModel::setPeriodType)
            PeriodSelector(
                label = formatPeriodRange(periodType, periodRange),
                onPrevious = viewModel::previousPeriod,
                onNext = viewModel::nextPeriod,
                onToday = viewModel::jumpToToday
            )

            when {
                substances.isEmpty() -> EmptyState("No substances yet. Add one in the Substances tab.")
                entries.isEmpty() -> EmptyState("No entries for this period.")
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item { SummarySection(summary) }
                    items(entries, key = { it.id }) { entry ->
                        EntryRow(
                            entry = entry,
                            substance = substancesById[entry.substanceId],
                            onClick = { editingEntry = entry; showEntryDialog = true },
                            onDelete = { deletingEntry = entry }
                        )
                    }
                }
            }
        }
    }

    if (showEntryDialog) {
        EntryEditorDialog(
            substances = substances,
            editing = editingEntry,
            onConfirm = { substanceId, quantity, price, source, comment, occurredAt ->
                viewModel.saveEntry(editingEntry, substanceId, quantity, price, source, comment, occurredAt)
                showEntryDialog = false
            },
            onDismiss = { showEntryDialog = false }
        )
    }

    deletingEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { deletingEntry = null },
            title = { Text("Delete this entry?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    deletingEntry = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deletingEntry = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodTypeToggle(selected: PeriodType, onSelect: (PeriodType) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = selected == PeriodType.WEEK, onClick = { onSelect(PeriodType.WEEK) }, label = { Text("Week") })
        FilterChip(selected = selected == PeriodType.MONTH, onClick = { onSelect(PeriodType.MONTH) }, label = { Text("Month") })
    }
}

@Composable
private fun PeriodSelector(label: String, onPrevious: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous period") }
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Row {
            IconButton(onClick = onToday) { Icon(Icons.Filled.Today, contentDescription = "Jump to today") }
            IconButton(onClick = onNext) { Icon(Icons.Filled.ChevronRight, contentDescription = "Next period") }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SummarySection(summary: List<SubstanceTotal>) {
    if (summary.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Totals", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            summary.forEach { total ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LabelDot(total.substance.colorArgb)
                        Text(
                            text = total.substance.name,
                            modifier = Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        text = "${formatQuantity(total.totalQuantity)} ${total.substance.unit} · ${formatMoney(total.totalPrice)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            val grandTotal = summary.sumOf { it.totalPrice }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("Total spent: ${formatMoney(grandTotal)}", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun EntryRow(entry: SubstanceEntry, substance: Substance?, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        ListItem(
            modifier = Modifier.padding(horizontal = 4.dp),
            leadingContent = substance?.let { { LabelDot(it.colorArgb, dotSize = 20.dp) } },
            headlineContent = {
                Text(
                    text = "${substance?.name ?: "Unknown"} · ${formatQuantity(entry.quantity)} ${substance?.unit.orEmpty()}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Column {
                    Text("${formatMoney(entry.price)} · ${formatTimestamp(entry.occurredAt)}")
                    if (entry.source.isNotBlank()) {
                        Text("Source: ${entry.source}", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (entry.comment.isNotBlank()) {
                        Text(bodyPreview(entry.comment), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            },
            trailingContent = {
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete entry") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryEditorDialog(
    substances: List<Substance>,
    editing: SubstanceEntry?,
    onConfirm: (substanceId: Long, quantity: Double, price: Double, source: String, comment: String, occurredAt: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSubstance by remember {
        mutableStateOf(substances.find { it.id == editing?.substanceId } ?: substances.firstOrNull())
    }
    var quantityText by remember { mutableStateOf(editing?.quantity?.let { formatQuantity(it) } ?: "") }
    var priceText by remember { mutableStateOf(editing?.price?.let { formatMoney(it) } ?: "") }
    var source by remember { mutableStateOf(editing?.source ?: "") }
    var comment by remember { mutableStateOf(editing?.comment ?: "") }
    var occurredAt by remember { mutableStateOf(editing?.occurredAt ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val occurredDate = Instant.ofEpochMilli(occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
    val quantity = quantityText.toDoubleOrNull()
    val price = priceText.toDoubleOrNull()
    val isValid = selectedSubstance != null && quantity != null && quantity > 0 && price != null && price >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Log entry" else "Edit entry") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedSubstance?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Substance") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        substances.forEach { substance ->
                            DropdownMenuItem(
                                text = { Text(substance.name) },
                                onClick = { selectedSubstance = substance; expanded = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity" + (selectedSubstance?.unit?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: "")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showDatePicker = true }) {
                    Text("Date: ${formatDateChip(occurredDate)}")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(selectedSubstance!!.id, quantity!!, price!!, source, comment, occurredAt) }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = occurredDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        occurredAt = combineDateWithTimeOfDay(newDate, occurredAt)
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

private fun combineDateWithTimeOfDay(date: LocalDate, existingMillis: Long): Long {
    val zone = ZoneId.systemDefault()
    val existingTime = Instant.ofEpochMilli(existingMillis).atZone(zone).toLocalTime()
    return date.atTime(existingTime).atZone(zone).toInstant().toEpochMilli()
}
