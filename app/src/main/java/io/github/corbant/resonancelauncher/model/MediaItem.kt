package io.github.corbant.resonancelauncher.model

import androidx.compose.runtime.Immutable

@Immutable
data class WatchProvider(
    val id: Int,
    val name: String,
    val logoUrl: String,
    val packageName: String?
)

@Immutable
data class MediaItem(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: String,
    val overview: String
)

data class MediaDetails(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: String,
    val overview: String,
    val releaseYear: String,
    val runtimeOrSeasons: String,
    val genres: List<String>,
    val watchProviders: List<WatchProvider> = emptyList(),
    val trailerYoutubeKey: String? = null
)