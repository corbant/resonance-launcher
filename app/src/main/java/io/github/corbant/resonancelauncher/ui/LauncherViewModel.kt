package io.github.corbant.resonancelauncher.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.corbant.resonancelauncher.data.AppItem
import io.github.corbant.resonancelauncher.data.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)
    private val _appsState = MutableStateFlow<List<AppItem>>(emptyList())
    val appsState: StateFlow<List<AppItem>> = _appsState.asStateFlow()

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _appsState.value = repository.getInstalledTvApps()
        }
    }

    fun launchApp(app: AppItem) {
        app.launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(app.launchIntent)
    }

    fun launchPackage(packageName: String) {
        val intent = repository.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun launchPlayStore() {
        launchPackage(AppRepository.PACKAGE_PLAY_STORE)
    }

    fun launchTvSettings() {
        launchPackage(AppRepository.PACKAGE_TV_SETTINGS)
    }
}