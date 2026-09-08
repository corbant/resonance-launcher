package io.github.corbant.resonancelauncher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_settings")

data class LauncherConfig(
    val tmdbApiKey: String = "",
    val streamingAvailabilityApiKey: String = "",
    val showMediaPreviews: Boolean = true,
    val favoritePackageNames: List<String> = emptyList(),
    val hiddenPackageNames: List<String> = emptyList()
)

class LauncherPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val TMDB_API_KEY = stringPreferencesKey("tmdb_api_key")
        val STREAMING_AVAILABILITY_API_KEY = stringPreferencesKey("streaming_availability_api_key")
        val SHOW_PREVIEWS = booleanPreferencesKey("show_media_previews")
        val FAVORITE_APPS = stringPreferencesKey("favorite_apps")
        val HIDDEN_APPS = stringPreferencesKey("hidden_apps")
    }

    val configFlow: Flow<LauncherConfig> = context.dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        val favoriteAppsRaw = preferences[PreferencesKeys.FAVORITE_APPS] ?: ""
        val hiddenAppsRaw = preferences[PreferencesKeys.HIDDEN_APPS] ?: ""
        LauncherConfig(
            tmdbApiKey = preferences[PreferencesKeys.TMDB_API_KEY] ?: "",
            streamingAvailabilityApiKey = preferences[PreferencesKeys.STREAMING_AVAILABILITY_API_KEY]
                ?: "",
            showMediaPreviews = preferences[PreferencesKeys.SHOW_PREVIEWS] ?: true,
            favoritePackageNames = if (favoriteAppsRaw.isBlank()) emptyList() else favoriteAppsRaw.split(
                ","
            ),
            hiddenPackageNames = if (hiddenAppsRaw.isBlank()) emptyList() else hiddenAppsRaw.split(
                ","
            )
        )
    }

    suspend fun saveApiKeys(tmdbKey: String, streamingKey: String) {
        context.dataStore.edit { preferences ->
            if (tmdbKey.isNotBlank()) {
                preferences[PreferencesKeys.TMDB_API_KEY] = tmdbKey
            }
            if (streamingKey.isNotBlank()) {
                preferences[PreferencesKeys.STREAMING_AVAILABILITY_API_KEY] = streamingKey
            }
        }
    }

    suspend fun updateShowPreviews(showPreviews: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_PREVIEWS] = showPreviews
        }
    }

    suspend fun saveFavoritePackages(packageNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FAVORITE_APPS] = packageNames.joinToString(",")
        }
    }

    suspend fun toggleFavorite(packageName: String) {
        context.dataStore.edit { preferences ->
            val current =
                preferences[PreferencesKeys.FAVORITE_APPS]?.split(",")?.filter { it.isNotBlank() }
                    ?: emptyList()
            val updated = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
            preferences[PreferencesKeys.FAVORITE_APPS] = updated.joinToString(",")
        }
    }

    suspend fun moveFavorite(packageName: String, delta: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAVORITE_APPS]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableList() ?: return@edit
            val currentIndex = current.indexOf(packageName)
            if (currentIndex == -1) return@edit
            val targetIndex = currentIndex + delta
            if (targetIndex in current.indices) {
                current.removeAt(currentIndex)
                current.add(targetIndex, packageName)
                preferences[PreferencesKeys.FAVORITE_APPS] = current.joinToString(",")
            }
        }
    }

    suspend fun toggleHideApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current =
                preferences[PreferencesKeys.HIDDEN_APPS]?.split(",")?.filter { it.isNotBlank() }
                    ?: emptyList()
            val updated = if (current.contains(packageName)) {
                current - packageName
            } else {
                current + packageName
            }
            preferences[PreferencesKeys.HIDDEN_APPS] = updated.joinToString(",")
        }
    }

    suspend fun unhideApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val current =
                preferences[PreferencesKeys.HIDDEN_APPS]?.split(",")?.filter { it.isNotBlank() }
                    ?: emptyList()
            val updated = current - packageName
            preferences[PreferencesKeys.HIDDEN_APPS] = updated.joinToString(",")
        }
    }
}