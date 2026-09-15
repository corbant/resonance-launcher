package io.github.corbant.resonancelauncher.ui.features.details

import io.github.corbant.resonancelauncher.model.MediaDetails
import io.github.corbant.resonancelauncher.model.MediaItem

sealed interface DetailsUiState {
    data class Loading(val initialSummary: MediaItem?) : DetailsUiState

    data class Success(
        val details: MediaDetails,
        val showMediaPreviews: Boolean = true,
    ) : DetailsUiState

    data class Error(val message: String) : DetailsUiState
}
