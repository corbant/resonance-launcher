package io.github.corbant.resonancelauncher.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.corbant.resonancelauncher.data.repository.AppRepository
import io.github.corbant.resonancelauncher.data.repository.LauncherPreferencesRepository
import io.github.corbant.resonancelauncher.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appRepository: AppRepository,
    private val preferencesRepository: LauncherPreferencesRepository
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

                    val sections = listOf(
                        HomeSection.AppTray(title = "Favorites", apps = favoriteApps)
                    )

                    val currentBackdrop = (_uiState.value as? HomeUiState.Success)?.featuredBackdropUrl

                    HomeUiState.Success(
                        allApps = visibleApps,
                        favoritePackageNames = config.favoritePackageNames,
                        featuredBackdropUrl = currentBackdrop,
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
                currentState.copy(featuredBackdropUrl = mediaItem.backdropUrl)
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
    preferencesRepository: LauncherPreferencesRepository
) = viewModelFactory {
    initializer {
        HomeViewModel(appRepository, preferencesRepository)
    }
}