package com.dori.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.dori.app.data.NoteRepository
import com.dori.app.data.Substance
import com.dori.app.data.SubstanceEntry
import kotlin.reflect.KClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

enum class PeriodType { WEEK, MONTH }

/** [start, end) - end is exclusive, both dates. */
data class PeriodRange(val start: LocalDate, val end: LocalDate)

data class SubstanceTotal(val substance: Substance, val totalQuantity: Double, val totalPrice: Double)

class TrackerViewModel(private val repository: NoteRepository) : ViewModel() {

    val substances: StateFlow<List<Substance>> = repository.getAllSubstances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _periodType = MutableStateFlow(PeriodType.WEEK)
    val periodType: StateFlow<PeriodType> = _periodType

    private val _periodAnchor = MutableStateFlow(LocalDate.now())
    val periodAnchor: StateFlow<LocalDate> = _periodAnchor

    val periodRange: StateFlow<PeriodRange> = combine(_periodType, _periodAnchor, ::rangeFor)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), rangeFor(_periodType.value, _periodAnchor.value))

    @OptIn(ExperimentalCoroutinesApi::class)
    val entriesForPeriod: StateFlow<List<SubstanceEntry>> = periodRange
        .flatMapLatest { range ->
            val zone = ZoneId.systemDefault()
            repository.getEntriesBetween(
                range.start.atStartOfDay(zone).toInstant().toEpochMilli(),
                range.end.atStartOfDay(zone).toInstant().toEpochMilli()
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val periodSummary: StateFlow<List<SubstanceTotal>> = combine(entriesForPeriod, substances) { entries, subs ->
        val substancesById = subs.associateBy { it.id }
        entries.groupBy { it.substanceId }
            .mapNotNull { (substanceId, group) ->
                val substance = substancesById[substanceId] ?: return@mapNotNull null
                SubstanceTotal(substance, group.sumOf { it.quantity }, group.sumOf { it.price })
            }
            .sortedByDescending { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun rangeFor(type: PeriodType, anchor: LocalDate): PeriodRange = when (type) {
        PeriodType.WEEK -> {
            val start = anchor.minusDays((anchor.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
            PeriodRange(start, start.plusWeeks(1))
        }
        PeriodType.MONTH -> {
            val start = anchor.withDayOfMonth(1)
            PeriodRange(start, start.plusMonths(1))
        }
    }

    fun setPeriodType(type: PeriodType) {
        _periodType.value = type
    }

    fun previousPeriod() {
        _periodAnchor.value = when (_periodType.value) {
            PeriodType.WEEK -> _periodAnchor.value.minusWeeks(1)
            PeriodType.MONTH -> _periodAnchor.value.minusMonths(1)
        }
    }

    fun nextPeriod() {
        _periodAnchor.value = when (_periodType.value) {
            PeriodType.WEEK -> _periodAnchor.value.plusWeeks(1)
            PeriodType.MONTH -> _periodAnchor.value.plusMonths(1)
        }
    }

    fun jumpToToday() {
        _periodAnchor.value = LocalDate.now()
    }

    fun saveEntry(
        editing: SubstanceEntry?,
        substanceId: Long,
        quantity: Double,
        price: Double,
        source: String,
        comment: String,
        occurredAt: Long
    ) {
        val substance = substances.value.find { it.id == substanceId } ?: return
        if (quantity <= 0) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (editing == null) {
                repository.insertEntry(
                    SubstanceEntry(
                        substanceId = substance.id,
                        substanceUuid = substance.uuid,
                        quantity = quantity,
                        price = price,
                        source = source.trim(),
                        comment = comment.trim(),
                        occurredAt = occurredAt,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            } else {
                repository.updateEntry(
                    editing.copy(
                        substanceId = substance.id,
                        substanceUuid = substance.uuid,
                        quantity = quantity,
                        price = price,
                        source = source.trim(),
                        comment = comment.trim(),
                        occurredAt = occurredAt,
                        updatedAt = now
                    )
                )
            }
        }
    }

    fun deleteEntry(entry: SubstanceEntry) {
        viewModelScope.launch { repository.softDeleteEntryById(entry.id) }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return TrackerViewModel(repository) as T
        }
    }
}
