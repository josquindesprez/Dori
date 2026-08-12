package com.dori.app.data

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Tracks which local note ids currently have an open EditorViewModel.
 *
 * The autosave debounce means in-memory editor state can be ahead of what's
 * persisted. If sync applied a remote update to a note the user has open
 * right now, a still-pending local autosave could fire afterward and
 * silently overwrite it. The sync engine consults this before writing a
 * remote change to a note that's open, deferring it to the next sync pass
 * instead.
 */
object OpenEditorRegistry {
    private val openIds = MutableStateFlow<Set<Long>>(emptySet())

    fun register(id: Long) {
        openIds.value = openIds.value + id
    }

    fun unregister(id: Long) {
        openIds.value = openIds.value - id
    }

    fun isOpen(id: Long): Boolean = id in openIds.value
}
