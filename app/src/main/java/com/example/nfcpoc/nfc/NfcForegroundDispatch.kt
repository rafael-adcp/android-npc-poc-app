package com.example.nfcpoc.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build

class NfcForegroundDispatch(private val adapter: NfcAdapter?) {

    fun enable(activity: Activity) {
        val adapter = adapter ?: return
        val pendingIntent = PendingIntent.getActivity(
            activity,
            0,
            Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            pendingIntentFlags()
        )
        adapter.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    fun disable(activity: Activity) {
        adapter?.disableForegroundDispatch(activity)
    }

    private fun pendingIntentFlags(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
}
