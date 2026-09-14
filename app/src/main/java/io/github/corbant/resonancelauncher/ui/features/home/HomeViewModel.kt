package io.github.corbant.resonancelauncher.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.corbant.resonancelauncher.data.repository.AppRepository
import io.github.corbant.resonancelauncher.data.repository.LauncherPreferencesRepository
import io.github.corbant.resonancelauncher.data.repository.MediaRepository
import io.github.corbant.resonancelauncher.data.tmdb.StreamingProviderMapping
import io.github.corbant.resonancelauncher.data.tmdb.TmdbClient
import io.github.corbant.resonancelauncher.data.tmdb.toMediaItem
import io.github.corbant.resonancelauncher.model.MediaItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appRepository: AppRepository,
    private val preferencesRepository: LauncherPreferencesRepository,
    private val tmdbClient: TmdbClient,
    private val mediaRepository: MediaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHomeData()
    }

    private fun observeHomeData() {
        viewModelScope.launch {
            try {
                combine(
                    appRepository.observeInstalledApps(),
                    preferencesRepository.configFlow
                ) { apps, config ->
                    val visibleApps = apps.filter { it.packageName !in config.hiddenPackageNames }
                    val appsMap = visibleApps.associateBy { it.packageName }
                    val favoriteApps = if (config.favoritePackageNames.isEmpty()) {
                        visibleApps
                    } else {
                        config.favoritePackageNames.mapNotNull { appsMap[it] }
                    }

                    var featuredMovies: List<MediaItem>? = null
                    var featuredTvShows: List<MediaItem>? = null
                    if (!config.tmdbApiKey.isBlank()) {
                        val installedPackages = apps.map { it.packageName }
                        val providerIds =
                            StreamingProviderMapping.getProviderIdsForInstalledPackages(
                                installedPackages
                            )

                        coroutineScope {
                            val moviesDeferred = async {
                                tmdbClient.getFeaturedMoviesByProviders(
                                    apiKey = config.tmdbApiKey,
                                    providerIds = providerIds
                                )
                            }
                            val tvDeferred = async {
                                tmdbClient.getFeaturedTvShowsByProviders(
                                    apiKey = config.tmdbApiKey,
                                    providerIds = providerIds
                                )
                            }
                            featuredMovies = moviesDeferred.await().map { it.toMediaItem() }
                            featuredTvShows = tvDeferred.await().map { it.toMediaItem() }
                            featuredMovies?.let { mediaRepository.cacheSummaryItems(it) }
                            featuredTvShows?.let { mediaRepository.cacheSummaryItems(it) }
                        }
                    }

                    val sections = buildList {
                        add(HomeSection.AppTray(title = "Favorite Apps", apps = favoriteApps))

                        if (!featuredMovies.isNullOrEmpty()) {
                            add(
                                HomeSection.ImmersiveMediaContent(
                                    title = "Featured Movies",
                                    items = featuredMovies,
                                    mediaType = "movie"
                                )
                            )
                        }

                        if (!featuredTvShows.isNullOrEmpty()) {
                            add(
                                HomeSection.ImmersiveMediaContent(
                                    title = "Featured TV Shows",
                                    items = featuredTvShows,
                                    mediaType = "tv"
                                )
                            )
                        }
                    }

                    HomeUiState.Success(
                        allApps = visibleApps,
                        favoritePackageNames = config.favoritePackageNames,
                        featuredBackdropUrl = null,
                        contentSections = sections
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load home data")
            }
        }
    }

    fun onMediaFocused(mediaItem: MediaItem) {
        _uiState.update { currentState ->
            if (currentState is HomeUiState.Success) {
                currentState.copy(
                    featuredBackdropUrl = mediaItem.backdropUrl,
                )
            } else currentState
        }
    }

    fun onMediaUnfocused(mediaItem: MediaItem) {
        _uiState.update { currentState ->
            if (currentState is HomeUiState.Success && currentState.featuredBackdropUrl.equals(
                    mediaItem.backdropUrl
                )
            ) {
                currentState.copy(
                    featuredBackdropUrl = null,
                )
            } else currentState
        }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(packageName)
        }
    }

    fun moveFavoriteLeft(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.moveFavorite(packageName, -1)
        }
    }

    fun moveFavoriteRight(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.moveFavorite(packageName, 1)
        }
    }

    fun toggleHideApp(packageName: String) {
        viewModelScope.launch {
            preferencesRepository.toggleHideApp(packageName)
        }
    }
}

fun createHomeViewModelFactory(
    appRepository: AppRepository,
    preferencesRepository: LauncherPreferencesRepository,
    tmdbClient: TmdbClient,
    mediaRepository: MediaRepository
) = viewModelFactory {
    initializer {
        HomeViewModel(appRepository, preferencesRepository, tmdbClient, mediaRepository)
    }
}