package com.example.nfcpoc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nfcpoc.nfc.NfcReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NfcReaderEdgeCaseTest {

    // --- Action routing ---

    @Test
    fun techDiscoveredAction_extractsNdefPayload() {
        val intent = ndefIntent(NfcAdapter.ACTION_TECH_DISCOVERED, "tech-data")
        assertEquals("tech-data", NfcReader.extractValue(intent))
    }

    @Test
    fun tagDiscoveredAction_extractsNdefPayload() {
        val intent = ndefIntent(NfcAdapter.ACTION_TAG_DISCOVERED, "tag-data")
        assertEquals("tag-data", NfcReader.extractValue(intent))
    }

    // --- NDEF edge cases ---

    @Test
    fun multipleRecords_returnsFirstNonBlank() {
        val blank = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), " ".toByteArray())
        val valid = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), "second".toByteArray())
        val message = NdefMessage(arrayOf(blank, valid))
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(message))
        }
        assertEquals("second", NfcReader.extractValue(intent))
    }

    @Test
    fun noNdefNoTag_returnsNull() {
        val intent = Intent(NfcAdapter.ACTION_TAG_DISCOVERED)
        assertNull(NfcReader.extractValue(intent))
    }

    @Test
    fun multipleMessages_extractsFromFirst() {
        val msg1 = NdefMessage(arrayOf(
            NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), "first-msg".toByteArray())
        ))
        val msg2 = NdefMessage(arrayOf(
            NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), "second-msg".toByteArray())
        ))
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(msg1, msg2))
        }
        assertEquals("first-msg", NfcReader.extractValue(intent))
    }

    @Test
    fun allBlankRecords_noTag_returnsNull() {
        val blank1 = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), " ".toByteArray())
        val blank2 = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), "  ".toByteArray())
        val message = NdefMessage(arrayOf(blank1, blank2))
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(message))
        }
        assertNull(NfcReader.extractValue(intent))
    }

    @Test
    fun emptyNdefPayload_noTag_returnsNull() {
        val empty = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), byteArrayOf())
        val message = NdefMessage(arrayOf(empty))
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(message))
        }
        assertNull(NfcReader.extractValue(intent))
    }

    // --- Helpers ---

    private fun ndefIntent(action: String, payload: String): Intent {
        val record = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload.toByteArray())
        val message = NdefMessage(arrayOf(record))
        return Intent(action).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf<NdefMessage>(message))
        }
    }
}
