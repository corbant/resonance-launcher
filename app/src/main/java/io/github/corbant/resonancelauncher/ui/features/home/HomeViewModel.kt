package io.github.corbant.resonancelauncher.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.corbant.resonancelauncher.data.AppRepository
import io.github.corbant.resonancelauncher.model.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val appRepository: AppRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeApps()
    }

    private fun observeApps() {
        viewModelScope.launch {
            appRepository.observeInstalledApps().collect { apps ->
                _uiState.update { currentState ->
                    if (currentState is HomeUiState.Success) {
                        currentState.copy(
                            installedApps = apps,
                        )
                    } else {
                        currentState
                    }
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            try {
                val apps = appRepository.getInstalledApps()

                val sections = listOf(
                    HomeSection.AppTray(apps = apps),
                )

                _uiState.value = HomeUiState.Success(
                    installedApps = apps,
                    featuredBackdropUrl = null,
                    contentSections = sections
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.localizedMessage ?: "Failed to load home")
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
}

fun createHomeViewModelFactory(appRepository: AppRepository) = viewModelFactory {
    initializer {
        HomeViewModel(appRepository)
    }
}