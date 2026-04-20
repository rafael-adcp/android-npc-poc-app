package com.example.nfcpoc

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.nfcpoc.nfc.IntentRouter
import com.example.nfcpoc.nfc.NfcForegroundDispatch
import com.example.nfcpoc.nfc.NfcReader
import com.example.nfcpoc.ui.AppScaffold
import com.example.nfcpoc.ui.theme.NfcPocTheme
import com.example.nfcpoc.ui.viewmodel.TagViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TagViewModel by viewModels {
        val app = application as RepositoryProvider
        TagViewModel.Factory(app.repository)
    }

    private val foregroundDispatch by lazy { NfcForegroundDispatch(NfcAdapter.getDefaultAdapter(this)) }
    private val intentRouter by lazy { IntentRouter(NfcReader, viewModel::onTagRead) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NfcPocTheme { AppScaffold(viewModel = viewModel) } }
        intentRouter.route(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentRouter.route(intent)
    }

    override fun onResume() {
        super.onResume()
        foregroundDispatch.enable(this)
    }

    override fun onPause() {
        super.onPause()
        foregroundDispatch.disable(this)
    }
}
