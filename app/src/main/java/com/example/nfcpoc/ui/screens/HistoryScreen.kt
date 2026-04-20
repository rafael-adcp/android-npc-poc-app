package com.example.nfcpoc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.example.nfcpoc.data.local.TagEntity
import java.text.DateFormat
import java.util.Date

object HistoryTags {
    const val LIST = "history-list"
    const val EMPTY = "history-empty"
    const val CLEAR_BUTTON = "history-clear"
    const val CLEAR_CONFIRM = "history-clear-confirm"
    const val CLEAR_CANCEL = "history-clear-cancel"
    fun row(id: Long) = "history-row-$id"
}

@Composable
fun HistoryScreen(
    items: List<TagEntity>,
    onClear: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .semantics { testTag = HistoryTags.EMPTY },
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhuma leitura ainda")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        OutlinedButton(
            onClick = { showConfirm = true },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = HistoryTags.CLEAR_BUTTON }
        ) {
            Text("Limpar histórico (${items.size})")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp)
                .semantics { testTag = HistoryTags.LIST },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { entity ->
                Card(modifier = Modifier.semantics { testTag = HistoryTags.row(entity.id) }) {
                    Column(Modifier.padding(12.dp)) {
                        Text(entity.tagValue, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Status: ${entity.syncStatus}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            DateFormat.getDateTimeInstance().format(Date(entity.readAt)),
                            style = MaterialTheme.typography.bodySmall
                        )
                        entity.apiResponse?.let {
                            Text("API: $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Limpar histórico?") },
            text = { Text("Todas as ${items.size} leituras salvas serão removidas. Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onClear()
                    },
                    modifier = Modifier.semantics { testTag = HistoryTags.CLEAR_CONFIRM }
                ) { Text("Limpar") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirm = false },
                    modifier = Modifier.semantics { testTag = HistoryTags.CLEAR_CANCEL }
                ) { Text("Cancelar") }
            }
        )
    }
}
