package com.dori.desktop

import com.dori.app.viewmodel.PeriodRange
import com.dori.app.viewmodel.PeriodType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val timestampFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
private val dateChipFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
private val periodDayFormatter = DateTimeFormatter.ofPattern("MMM d")
private val periodDayWithYearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

fun formatTimestamp(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(timestampFormatter)

fun formatDateChip(date: LocalDate): String = date.format(dateChipFormatter)

fun bodyPreview(body: String): String =
    body.trim().replace(Regex("\\s+"), " ").take(160)

fun formatQuantity(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else String.format(Locale.US, "%.2f", value)

fun formatMoney(value: Double): String = String.format(Locale.US, "%.2f", value)

fun formatPeriodRange(type: PeriodType, range: PeriodRange): String = when (type) {
    PeriodType.WEEK -> {
        val lastDay = range.end.minusDays(1)
        "${range.start.format(periodDayFormatter)} – ${lastDay.format(periodDayWithYearFormatter)}"
    }
    PeriodType.MONTH -> range.start.format(monthFormatter)
}
