package com.example.nfcpoc.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.core.content.IntentCompat

object NfcReader {

    fun extractValue(intent: Intent): String? {
        if (!isNfcIntent(intent)) return null
        return extractNdefText(intent) ?: extractUid(intent)
    }

    private fun isNfcIntent(intent: Intent): Boolean = intent.action in NFC_ACTIONS

    private fun extractNdefText(intent: Intent): String? {
        val messages = IntentCompat.getParcelableArrayExtra(
            intent, NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java
        ) ?: return null
        return messages.asSequence()
            .filterIsInstance<NdefMessage>()
            .flatMap { it.records.asSequence() }
            .mapNotNull { record ->
                runCatching { String(record.payload, Charsets.UTF_8) }.getOrNull()
            }
            .firstOrNull { it.isNotBlank() }
    }

    private fun extractUid(intent: Intent): String? {
        val tag = IntentCompat.getParcelableExtra(
            intent, NfcAdapter.EXTRA_TAG, Tag::class.java
        ) ?: return null
        return tag.id?.toHexString()
    }

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { byte -> "%02X".format(byte) }

    private val NFC_ACTIONS = setOf(
        NfcAdapter.ACTION_NDEF_DISCOVERED,
        NfcAdapter.ACTION_TECH_DISCOVERED,
        NfcAdapter.ACTION_TAG_DISCOVERED
    )
}
