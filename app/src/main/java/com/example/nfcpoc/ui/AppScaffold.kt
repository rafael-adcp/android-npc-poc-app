package com.example.nfcpoc.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.example.nfcpoc.BuildConfig
import com.example.nfcpoc.ui.screens.HistoryScreen
import com.example.nfcpoc.ui.screens.ReadTagScreen
import com.example.nfcpoc.ui.viewmodel.TagViewModel

object AppTags {
    const val TAB_READ = "tab-read"
    const val TAB_HISTORY = "tab-history"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    viewModel: TagViewModel,
    debugEnabled: Boolean = BuildConfig.DEBUG,
    apiBaseUrl: String = BuildConfig.API_BASE_URL
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val readState by viewModel.readState.collectAsState()
    val history by viewModel.history.collectAsState()
    val lastRawValue by viewModel.lastRawValue.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Nfc, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("NFC POC")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabBar(selected = selected, onTabSelected = { selected = it })

            if (debugEnabled) {
                DebugInfoPanel(
                    info = DebugInfo(apiBaseUrl = apiBaseUrl, lastRawValue = lastRawValue)
                )
            }

            when (selected) {
                0 -> ReadTagScreen(state = readState, onReset = viewModel::resetState)
                else -> HistoryScreen(items = history, onClear = viewModel::clearHistory)
            }
        }
    }
}

@Composable
private fun TabBar(selected: Int, onTabSelected: (Int) -> Unit) {
    TabRow(selectedTabIndex = selected) {
        Tab(
            selected = selected == 0,
            onClick = { onTabSelected(0) },
            text = { Text("Ler") },
            modifier = Modifier.semantics { testTag = AppTags.TAB_READ }
        )
        Tab(
            selected = selected == 1,
            onClick = { onTabSelected(1) },
            text = { Text("Histórico") },
            modifier = Modifier.semantics { testTag = AppTags.TAB_HISTORY }
        )
    }
}
