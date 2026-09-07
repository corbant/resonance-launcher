package io.github.corbant.resonancelauncher.ui.features.settings

import android.graphics.Bitmap

sealed interface PairingModalState {
    data object Idle : PairingModalState
    data object Loading : PairingModalState
    data class Active(val serverUrl: String, val qrBitmap: Bitmap) : PairingModalState
    data class Error(val message: String) : PairingModalState
}

data class SettingsUiState(
    val pairingModal: PairingModalState = PairingModalState.Idle
)