package com.dori.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * A process-wide scope for the final autosave flush issued from
 * EditorViewModel.onCleared(). viewModelScope is cancelled as part of
 * clearing the ViewModel, so the last write must run on a scope that
 * outlives it.
 */
object AppScope {
    val io: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
