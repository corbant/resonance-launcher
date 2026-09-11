package io.github.corbant.resonancelauncher.model

import androidx.compose.runtime.Immutable

@Immutable
data class MediaItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: String,
    val overview: String
)