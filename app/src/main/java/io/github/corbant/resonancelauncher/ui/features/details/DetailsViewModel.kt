package io.github.corbant.resonancelauncher.ui.features.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import io.github.corbant.resonancelauncher.data.repository.AppRepository
import io.github.corbant.resonancelauncher.data.repository.LauncherPreferencesRepository
import io.github.corbant.resonancelauncher.data.repository.MediaRepository
import io.github.corbant.resonancelauncher.model.MediaItem
import io.github.corbant.resonancelauncher.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val mediaRepository: MediaRepository,
    private val appRepository: AppRepository,
    private val preferencesRepository: LauncherPreferencesRepository
) : ViewModel() {

    private val route: Route.MediaDetails? = try {
        savedStateHandle.toRoute<Route.MediaDetails>()
    } catch (_: Exception) {
        null
    }

    private val mediaId: Int = route?.mediaId ?: checkNotNull(savedStateHandle.get<Int>("mediaId"))
    private val mediaType: String = route?.mediaType ?: checkNotNull(savedStateHandle.get<String>("mediaType"))

    private val initialSummary: MediaItem? = mediaRepository.getCachedItem(mediaId)

    private val _uiState = MutableStateFlow<DetailsUiState>(
        DetailsUiState.Loading(initialSummary)
    )
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        loadFullDetails()
    }

    fun retry() {
        _uiState.value = DetailsUiState.Loading(initialSummary)
        loadFullDetails()
    }

    private fun loadFullDetails() {
        viewModelScope.launch {
            try {
                val config = preferencesRepository.configFlow.first()
                val apiKey = config.tmdbApiKey
                if (apiKey.isBlank()) {
                    _uiState.value = DetailsUiState.Error("TMDB API Key is missing. Please set it in Settings.")
                    return@launch
                }

                val tmdbDetails = mediaRepository.fetchDetails(apiKey, mediaId, mediaType)

                val installedApps = appRepository.getInstalledApps()
                val installedPackages = installedApps.map { it.packageName }.toSet()

                val providers = try {
                    mediaRepository.resolveWatchProviders(
                        apiKey = apiKey,
                        mediaId = mediaId,
                        mediaType = mediaType,
                        installedPackages = installedPackages
                    )
                } catch (_: Exception) {
                    emptyList()
                }

                _uiState.value = DetailsUiState.Success(
                    details = tmdbDetails.copy(
                        watchProviders = providers
                    )
                )
            } catch (e: Exception) {
                _uiState.value = DetailsUiState.Error(e.message ?: "Failed to load details")
            }
        }
    }
}

fun createDetailsViewModelFactory(
    mediaRepository: MediaRepository,
    appRepository: AppRepository,
    preferencesRepository: LauncherPreferencesRepository
) = viewModelFactory {
    initializer {
        val savedStateHandle = createSavedStateHandle()
        DetailsViewModel(
            savedStateHandle = savedStateHandle,
            mediaRepository = mediaRepository,
            appRepository = appRepository,
            preferencesRepository = preferencesRepository
        )
    }
}
