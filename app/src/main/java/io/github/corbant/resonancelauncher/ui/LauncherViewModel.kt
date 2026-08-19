package io.github.corbant.resonancelauncher.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.corbant.resonancelauncher.data.AppRepository
import io.github.corbant.resonancelauncher.data.TvAppItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application): AndroidViewModel(application) {
    private val repository = AppRepository(application)

    private val _appsState = MutableStateFlow<List<TvAppItem>>(emptyList())
    val appsState: StateFlow<List<TvAppItem>> = _appsState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _appsState.value = repository.getInstalledTvApps()
        }
    }

    fun launchApp(app: TvAppItem) {
        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(app.launchIntent)
    }
}