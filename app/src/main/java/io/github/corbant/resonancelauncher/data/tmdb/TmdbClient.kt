package io.github.corbant.resonancelauncher.data.tmdb

import io.github.corbant.resonancelauncher.model.MediaItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TmdbDiscoverResponse(
    val results: List<TmdbMediaDto> = emptyList()
)

@Serializable
data class TmdbMediaDto(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("overview") val overview: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null
)

@Serializable
data class TmdbDetailsDto(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("first_air_date") val firstAirDate: String? = null,
    val runtime: Int? = null,
    @SerialName("number_of_seasons") val numberOfSeasons: Int? = null,
    val genres: List<TmdbGenreDto>? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
    val videos: TmdbVideosContainerDto? = null
)

@Serializable
data class TmdbGenreDto(
    val id: Int,
    val name: String
)

@Serializable
data class TmdbVideosContainerDto(
    val results: List<TmdbVideoDto> = emptyList()
)

@Serializable
data class TmdbVideoDto(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean? = null,
    @SerialName("iso_639_1") val iso6391: String? = null
)

@Serializable
data class TmdbWatchProvidersResponse(
    val id: Int,
    val results: Map<String, TmdbRegionProvidersDto> = emptyMap()
)

@Serializable
data class TmdbRegionProvidersDto(
    val link: String? = null,
    val flatrate: List<TmdbProviderDto>? = null,
    val rent: List<TmdbProviderDto>? = null,
    val buy: List<TmdbProviderDto>? = null,
    val ads: List<TmdbProviderDto>? = null
)

@Serializable
data class TmdbProviderDto(
    @SerialName("provider_id") val providerId: Int,
    @SerialName("provider_name") val providerName: String,
    @SerialName("logo_path") val logoPath: String? = null,
    @SerialName("display_priority") val displayPriority: Int? = null
)

fun TmdbMediaDto.toMediaItem(): MediaItem {
    val imageBaseUrl = "https://image.tmdb.org/t/p/"
    return MediaItem(
        id = id,
        title = title ?: name ?: "Untitled",
        posterUrl = posterPath?.let { "${imageBaseUrl}w500$it" },
        backdropUrl = backdropPath?.let { "${imageBaseUrl}original$it" },
        rating = voteAverage?.let { String.format("%.1f", it) } ?: "",
        overview = overview ?: ""
    )
}

class TmdbClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = "https://api.themoviedb.org/3"

    suspend fun getMediaDetails(apiKey: String, mediaId: Int, mediaType: String): TmdbDetailsDto =
        withContext(Dispatchers.IO) {
            client.get("$baseUrl/$mediaType/$mediaId") {
                parameter("api_key", apiKey)
                parameter("append_to_response", "videos")
            }.body()
        }

    suspend fun getWatchProviders(
        apiKey: String,
        mediaId: Int,
        mediaType: String,
        region: String = "US"
    ): List<TmdbProviderDto> = withContext(Dispatchers.IO) {
        try {
            val response: TmdbWatchProvidersResponse =
                client.get("$baseUrl/$mediaType/$mediaId/watch/providers") {
                    parameter("api_key", apiKey)
                }.body()

            val regionData = response.results[region] ?: return@withContext emptyList()

            val streamingProviders = mutableListOf<TmdbProviderDto>()
            regionData.flatrate?.let { streamingProviders.addAll(it) }
            regionData.ads?.let { adsProviders ->
                val existingIds = streamingProviders.map { it.providerId }.toSet()
                streamingProviders.addAll(adsProviders.filter { it.providerId !in existingIds })
            }

            streamingProviders.sortedBy { it.displayPriority ?: Int.MAX_VALUE }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFeaturedMoviesByProviders(
        apiKey: String,
        providerIds: List<Int>,
        region: String = "US",
        monetizationTypes: List<String> = listOf("flatrate", "free", "ads")
    ): List<TmdbMediaDto> {
        return discoverMedia(
            type = "movie",
            apiKey = apiKey,
            providerIds = providerIds,
            region = region,
            monetizationTypes = monetizationTypes
        )
    }

    suspend fun getFeaturedTvShowsByProviders(
        apiKey: String,
        providerIds: List<Int>,
        region: String = "US",
        monetizationTypes: List<String> = listOf("flatrate", "free", "ads")
    ): List<TmdbMediaDto> {
        return discoverMedia(
            type = "tv",
            apiKey = apiKey,
            providerIds = providerIds,
            region = region,
            monetizationTypes = monetizationTypes
        )
    }

    private suspend fun discoverMedia(
        type: String,
        apiKey: String,
        providerIds: List<Int>,
        region: String,
        monetizationTypes: List<String>
    ): List<TmdbMediaDto> {
        if (apiKey.isBlank() || providerIds.isEmpty()) return emptyList()

        val providerPipeSeparated = providerIds.joinToString("|")
        val monetizationPipeSeparated = monetizationTypes.joinToString("|")

        return try {
            val response: TmdbDiscoverResponse =
                client.get("$baseUrl/discover/$type") {
                    parameter("api_key", apiKey)
                    parameter("watch_region", region)
                    parameter("with_watch_providers", providerPipeSeparated)
                    if (monetizationPipeSeparated.isNotBlank()) {
                        parameter("with_watch_monetization_types", monetizationPipeSeparated)
                    }
                    parameter("sort_by", "popularity.desc")
                    parameter("vote_count.gte", 100)
                }.body()

            response.results
        } catch (e: Exception) {
            emptyList()
        }
    }
}