package com.example.nfcpoc.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.example.nfcpoc.ui.viewmodel.ReadUiState

object ReadTagTags {
    const val ROOT = "read-root"
    const val STATUS = "read-status"
    const val RESET = "read-reset"
}

@Composable
fun ReadTagScreen(
    state: ReadUiState,
    onReset: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(24.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .semantics { testTag = ReadTagTags.ROOT },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val (title, detail) = when (state) {
            ReadUiState.Idle -> "Aproxime o cartão do celular" to null
            ReadUiState.Loading -> "Processando leitura…" to null
            is ReadUiState.Success -> "Leitura salva" to "${state.entity.tagValue}\n${state.entity.apiResponse ?: ""}"
            is ReadUiState.Error -> "Falha" to state.message
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { testTag = ReadTagTags.STATUS }
        )

        if (detail != null) {
            Spacer(Modifier.height(16.dp))
            Text(detail, style = MaterialTheme.typography.bodyMedium)
        }

        if (state is ReadUiState.Loading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }

        if (state is ReadUiState.Success || state is ReadUiState.Error) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onReset,
                modifier = Modifier.semantics { testTag = ReadTagTags.RESET }
            ) {
                Text("Ler outro cartão")
            }
        }
    }
}
