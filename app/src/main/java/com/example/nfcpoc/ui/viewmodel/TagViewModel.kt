package com.example.nfcpoc.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nfcpoc.data.local.TagEntity
import com.example.nfcpoc.data.repository.TagProcessingException
import com.example.nfcpoc.data.repository.TagRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ReadUiState {
    data object Idle : ReadUiState
    data object Loading : ReadUiState
    data class Success(val entity: TagEntity) : ReadUiState
    data class Error(val message: String, val persisted: TagEntity?) : ReadUiState
}

class TagViewModel(private val repository: TagRepository) : ViewModel() {

    private val _readState = MutableStateFlow<ReadUiState>(ReadUiState.Idle)
    val readState: StateFlow<ReadUiState> = _readState.asStateFlow()

    private val _lastRawValue = MutableStateFlow<String?>(null)
    val lastRawValue: StateFlow<String?> = _lastRawValue.asStateFlow()

    val history: StateFlow<List<TagEntity>> = repository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onTagRead(value: String) {
        if (value.isBlank()) return
        _lastRawValue.value = value
        _readState.value = ReadUiState.Loading
        viewModelScope.launch {
            val result = repository.processTag(value)
            _readState.value = result.fold(
                onSuccess = { ReadUiState.Success(it) },
                onFailure = { error ->
                    val persisted = (error as? TagProcessingException)?.persistedEntity
                    ReadUiState.Error(error.message ?: "Falha ao processar", persisted)
                }
            )
        }
    }

    fun resetState() {
        _readState.value = ReadUiState.Idle
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _lastRawValue.value = null
            _readState.value = ReadUiState.Idle
        }
    }

    class Factory(private val repository: TagRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TagViewModel(repository) as T
    }
}
