package io.github.corbant.resonancelauncher.ui.features.settings

import android.graphics.Bitmap
import io.github.corbant.resonancelauncher.data.repository.LauncherConfig
import io.github.corbant.resonancelauncher.model.AppItem

sealed interface PairingModalState {
    data object Idle : PairingModalState
    data object Loading : PairingModalState
    data class Active(val serverUrl: String, val qrBitmap: Bitmap) : PairingModalState
    data class Error(val message: String) : PairingModalState
}

data class SettingsUiState(
    val config: LauncherConfig = LauncherConfig(),
    val hiddenApps: List<AppItem> = emptyList(),
    val isHiddenAppsModalOpen: Boolean = false,
    val pairingModal: PairingModalState = PairingModalState.Idle
)