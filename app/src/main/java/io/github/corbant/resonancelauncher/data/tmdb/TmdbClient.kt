package io.github.corbant.resonancelauncher.data.tmdb

import io.github.corbant.resonancelauncher.model.MediaItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
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

    suspend fun getFeaturedByProviders(
        apiKey: String,
        providerIds: List<Int>,
        region: String = "US"
    ): List<TmdbMediaDto> {
        if (apiKey.isBlank() || providerIds.isEmpty()) return emptyList()

        val providerPipeSeparated = providerIds.joinToString("|")

        return try {
            val response: TmdbDiscoverResponse =
                client.get("https://api.themoviedb.org/3/discover/movie") {
                    parameter("api_key", apiKey)
                    parameter("watch_region", region)
                    parameter("with_watch_providers", providerPipeSeparated)
                    parameter("sort_by", "popularity.desc")
                    parameter("vote_count.gte", 100)
                }.body()

            response.results
        } catch (e: Exception) {
            emptyList()
        }
    }
}