package com.dori.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.dori.app.data.NoteRepository
import com.dori.app.data.Substance
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SubstancesViewModel(private val repository: NoteRepository) : ViewModel() {

    val substances: StateFlow<List<Substance>> = repository.getAllSubstances()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSubstance(name: String, unit: String, colorArgb: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertSubstance(Substance(name = name.trim(), unit = unit.trim(), colorArgb = colorArgb))
        }
    }

    fun updateSubstance(substance: Substance, name: String, unit: String, colorArgb: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateSubstance(substance.copy(name = name.trim(), unit = unit.trim(), colorArgb = colorArgb))
        }
    }

    fun deleteSubstance(substance: Substance) {
        viewModelScope.launch { repository.deleteSubstance(substance) }
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            return SubstancesViewModel(repository) as T
        }
    }
}
