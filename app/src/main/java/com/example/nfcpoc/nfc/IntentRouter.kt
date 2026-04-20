package com.example.nfcpoc.nfc

import android.content.Intent

class IntentRouter(
    private val tagReader: NfcReader,
    private val onTagRead: (String) -> Unit
) {
    fun route(intent: Intent?) {
        if (intent == null) return
        val value = tagReader.extractValue(intent) ?: return
        onTagRead(value)
    }
}
