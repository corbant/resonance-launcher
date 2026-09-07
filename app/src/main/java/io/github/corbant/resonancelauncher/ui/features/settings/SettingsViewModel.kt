package io.github.corbant.resonancelauncher.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.corbant.resonancelauncher.data.server.SetupServerManager
import io.github.corbant.resonancelauncher.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val serverManager: SetupServerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var serverJob: Job? = null

    fun openConfigModal() {
        if (_uiState.value.pairingModal !is PairingModalState.Idle) return

        _uiState.update { it.copy(pairingModal = PairingModalState.Loading) }

        serverJob = viewModelScope.launch {
            try {
                val url = serverManager.startServer { key, trakt ->
                    // TODO: handle incoming data
                }

                val qrBitmap = withContext(Dispatchers.Default) {
                    QrCodeGenerator.generateQrBitmap(url)
                }

                _uiState.update {
                    it.copy(pairingModal = PairingModalState.Active(url, qrBitmap))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        pairingModal = PairingModalState.Error(
                            e.message ?: "Failed to start server"
                        )
                    )
                }
            }
        }
    }

    fun closeConfigModal() {
        serverJob?.cancel()
        serverJob = null
        serverManager.stopServer()
        _uiState.update { it.copy(pairingModal = PairingModalState.Idle) }
    }

    override fun onCleared() {
        serverManager.stopServer()
    }
}

fun createSettingsViewModelFactory(serverManager: SetupServerManager) = viewModelFactory {
    initializer {
        SettingsViewModel(serverManager)
    }
}
