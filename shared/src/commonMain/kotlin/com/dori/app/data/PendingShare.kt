package com.dori.app.data

/**
 * One-shot inbox for text handed to Dori by another app (e.g. a quote sent
 * from Research Reader via ACTION_SEND). The Android entry point stores the
 * shared text here just before navigating to the editor; EditorViewModel
 * consumes - and clears - it exactly once, when it initializes a brand new
 * note. Never populated on desktop, so it's simply always empty there.
 */
object PendingShare {
    private var text: String? = null

    fun offer(value: String) {
        text = value
    }

    fun consume(): String? {
        val value = text
        text = null
        return value
    }
}
