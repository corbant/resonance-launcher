package io.github.corbant.resonancelauncher.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.corbant.resonancelauncher.data.repository.AppRepository
import io.github.corbant.resonancelauncher.data.repository.LauncherPreferencesRepository
import io.github.corbant.resonancelauncher.data.server.SetupServerManager
import io.github.corbant.resonancelauncher.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val serverManager: SetupServerManager,
    private val preferencesRepository: LauncherPreferencesRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var serverJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                appRepository.observeInstalledApps(),
                preferencesRepository.configFlow
            ) { apps, config ->
                val appsMap = apps.associateBy { it.packageName }
                val hiddenApps = config.hiddenPackageNames.mapNotNull { appsMap[it] }
                Pair(config, hiddenApps)
            }.collect { (config, hiddenApps) ->
                _uiState.update { it.copy(config = config, hiddenApps = hiddenApps) }
            }
        }
    }

    fun toggleShowMediaPreviews() {
        viewModelScope.launch {
            val current = _uiState.value.config.showMediaPreviews
            preferencesRepository.updateShowPreviews(!current)
        }
    }

    fun unhideApp(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.unhideApp(packageName)
        }
    }

    fun openHiddenAppsModal() {
        _uiState.update { it.copy(isHiddenAppsModalOpen = true) }
    }

    fun closeHiddenAppsModal() {
        _uiState.update { it.copy(isHiddenAppsModalOpen = false) }
    }

    fun openConfigModal() {
        if (_uiState.value.pairingModal !is PairingModalState.Idle) return

        _uiState.update { it.copy(pairingModal = PairingModalState.Loading) }

        serverJob = viewModelScope.launch {
            try {
                val url = serverManager.startServer { tmdbKey, streamingKey ->
                    viewModelScope.launch {
                        preferencesRepository.saveApiKeys(tmdbKey, streamingKey)
                    }
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

fun createSettingsViewModelFactory(
    serverManager: SetupServerManager,
    preferencesRepository: LauncherPreferencesRepository,
    appRepository: AppRepository
) = viewModelFactory {
    initializer {
        SettingsViewModel(serverManager, preferencesRepository, appRepository)
    }
}