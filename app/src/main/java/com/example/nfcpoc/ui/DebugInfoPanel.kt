package com.example.nfcpoc.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp

object DebugPanelTags {
    const val ROOT = "debug-panel"
    const val API_URL = "debug-api-url"
    const val LAST_VALUE = "debug-last-value"
}

data class DebugInfo(
    val apiBaseUrl: String,
    val lastRawValue: String?
)

@Composable
fun DebugInfoPanel(info: DebugInfo, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { testTag = DebugPanelTags.ROOT },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("DEBUG", style = MaterialTheme.typography.labelSmall)
            Text(
                "API: ${info.apiBaseUrl}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics { testTag = DebugPanelTags.API_URL }
            )
            Text(
                "Último valor lido: ${info.lastRawValue ?: "—"}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.semantics { testTag = DebugPanelTags.LAST_VALUE }
            )
        }
    }
}
