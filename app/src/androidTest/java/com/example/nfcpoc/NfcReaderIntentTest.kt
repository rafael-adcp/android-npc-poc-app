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
class NfcReaderIntentTest {

    @Test
    fun extractsNdefPayloadFromIntent() {
        val payload = "hello-tag".toByteArray()
        val record = NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, byteArrayOf(), payload)
        val message = NdefMessage(arrayOf(record))
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf<NdefMessage>(message))
        }

        val value = NfcReader.extractValue(intent)
        assertEquals("hello-tag", value)
    }

    @Test
    fun nonNfcIntent_returnsNull() {
        val intent = Intent(Intent.ACTION_VIEW)
        assertNull(NfcReader.extractValue(intent))
    }
}
