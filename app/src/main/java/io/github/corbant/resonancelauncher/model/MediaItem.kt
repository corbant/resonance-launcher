package io.github.corbant.resonancelauncher.model

import androidx.compose.runtime.Immutable

@Immutable
data class MediaItem(
    val title: String,
    val backdropUrl: String
)