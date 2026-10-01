package io.github.corbant.resonancelauncher.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class WatchHistoryItem(
    val id: Int,
    val title: String,
    val mediaType: String,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val lastWatchedTimestamp: Long = System.currentTimeMillis(),
    val providerPackageName: String? = null,
    val providerName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val progressPercentage: Float? = null,
    val nextEpisodeTitle: String? = null
)
