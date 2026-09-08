package io.github.corbant.resonancelauncher.ui.features.home

import io.github.corbant.resonancelauncher.model.AppItem
import io.github.corbant.resonancelauncher.model.MediaItem
import io.github.corbant.resonancelauncher.model.WatchHistoryItem

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val allApps: List<AppItem> = emptyList(),
        val favoritePackageNames: List<String> = emptyList(),
        val featuredBackdropUrl: String? = null,
        val contentSections: List<HomeSection> = emptyList()
    ) : HomeUiState

    data class Error(val message: String) : HomeUiState
}

sealed interface HomeSection {
    val title: String

    data class AppTray(
        override val title: String = "Apps",
        val apps: List<AppItem>
    ) : HomeSection

    data class MediaContent(
        override val title: String,
        val items: List<MediaItem>,
    ) : HomeSection

    data class ContinueWatching(
        override val title: String,
        val items: List<WatchHistoryItem>
    ) : HomeSection
}