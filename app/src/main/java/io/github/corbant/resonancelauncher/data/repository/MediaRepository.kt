package io.github.corbant.resonancelauncher.data.repository

import io.github.corbant.resonancelauncher.data.tmdb.StreamingProviderMapping
import io.github.corbant.resonancelauncher.data.tmdb.TmdbClient
import io.github.corbant.resonancelauncher.data.tmdb.TmdbVideoDto
import io.github.corbant.resonancelauncher.model.MediaDetails
import io.github.corbant.resonancelauncher.model.MediaItem
import io.github.corbant.resonancelauncher.model.WatchProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class MediaRepository(
    private val tmdbClient: TmdbClient
) {
    private val summaryCache = ConcurrentHashMap<Int, MediaItem>()

    fun cacheSummaryItems(items: List<MediaItem>) {
        items.forEach { summaryCache[it.id] = it }
    }

    fun getCachedItem(id: Int): MediaItem? = summaryCache[id]

    suspend fun fetchDetails(
        apiKey: String,
        mediaId: Int,
        mediaType: String
    ): MediaDetails = withContext(Dispatchers.IO) {
        val dto = tmdbClient.getMediaDetails(apiKey, mediaId, mediaType)
        val releaseDate = dto.releaseDate ?: dto.firstAirDate ?: ""
        val year = if (releaseDate.length >= 4) releaseDate.substring(0, 4) else ""

        val runtimeString = when {
            dto.runtime != null && dto.runtime > 0 -> "${dto.runtime / 60}h ${dto.runtime % 60}m"
            dto.numberOfSeasons != null -> "${dto.numberOfSeasons} Season${if (dto.numberOfSeasons > 1) "s" else ""}"
            else -> ""
        }

        val trailerKey = dto.videos?.results.orEmpty()
            .filter {
                it.site.equals("YouTube", ignoreCase = true) &&
                        (it.type.equals("Trailer", ignoreCase = true) || it.type.equals(
                            "Teaser",
                            ignoreCase = true
                        ))
            }
            .sortedWith(
                compareByDescending<TmdbVideoDto> { it.official == true }
                    .thenByDescending { it.type.equals("Trailer", ignoreCase = true) }
                    .thenByDescending { it.type.equals("Teaser", ignoreCase = true) }
            )
            .firstOrNull()?.key

        MediaDetails(
            id = dto.id,
            title = dto.title ?: dto.name ?: "Untitled",
            overview = dto.overview.orEmpty(),
            posterUrl = dto.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
            backdropUrl = dto.backdropPath?.let { "https://image.tmdb.org/t/p/original$it" },
            releaseYear = year,
            runtimeOrSeasons = runtimeString,
            genres = dto.genres?.map { it.name } ?: emptyList(),
            rating = dto.voteAverage?.let { String.format("%.1f", it) }.orEmpty(),
            watchProviders = emptyList(),
            trailerYoutubeKey = trailerKey
        )
    }

    suspend fun resolveWatchProviders(
        apiKey: String,
        mediaId: Int,
        mediaType: String,
        installedPackages: Set<String>,
        region: String = "US"
    ): List<WatchProvider> = withContext(Dispatchers.IO) {

        val providerDtos = tmdbClient.getWatchProviders(apiKey, mediaId, mediaType, region)

        providerDtos.mapNotNull { provider ->
            val pkg = StreamingProviderMapping.getPackageForProviderId(provider.providerId)

            if (pkg != null && installedPackages.contains(pkg)) {
                WatchProvider(
                    id = provider.providerId,
                    name = provider.providerName,
                    logoUrl = "https://image.tmdb.org/t/p/w200${provider.logoPath}",
                    packageName = pkg
                )
            } else {
                null
            }
        }
    }
}