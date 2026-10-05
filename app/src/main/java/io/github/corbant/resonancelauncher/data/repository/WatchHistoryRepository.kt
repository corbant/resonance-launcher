package io.github.corbant.resonancelauncher.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.corbant.resonancelauncher.model.WatchHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.watchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "watch_history_settings")

class WatchHistoryRepository(private val context: Context) {

    private object PreferencesKeys {
        val WATCH_HISTORY = stringPreferencesKey("watch_history_json")
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val watchHistoryFlow: Flow<List<WatchHistoryItem>> = context.watchHistoryDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val jsonString = preferences[PreferencesKeys.WATCH_HISTORY] ?: ""
            if (jsonString.isBlank()) {
                emptyList()
            } else {
                try {
                    val list: List<WatchHistoryItem> = json.decodeFromString(jsonString)
                    list.sortedByDescending { it.lastWatchedTimestamp }
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }

    suspend fun recordWatch(item: WatchHistoryItem) {
        context.watchHistoryDataStore.edit { preferences ->
            val jsonString = preferences[PreferencesKeys.WATCH_HISTORY] ?: ""
            val currentList: MutableList<WatchHistoryItem> = if (jsonString.isNotBlank()) {
                try {
                    json.decodeFromString<List<WatchHistoryItem>>(jsonString).toMutableList()
                } catch (_: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            // Remove existing item if present (match by id & mediaType)
            currentList.removeAll { it.id == item.id && it.mediaType.equals(item.mediaType, ignoreCase = true) }

            // Add updated item to top with current timestamp
            val updatedItem = item.copy(lastWatchedTimestamp = System.currentTimeMillis())
            currentList.add(0, updatedItem)

            // Limit to top 30 items
            val trimmedList = currentList.take(30)
            preferences[PreferencesKeys.WATCH_HISTORY] = json.encodeToString(trimmedList)
        }
    }

    suspend fun removeFromHistory(id: Int, mediaType: String) {
        context.watchHistoryDataStore.edit { preferences ->
            val jsonString = preferences[PreferencesKeys.WATCH_HISTORY] ?: ""
            if (jsonString.isNotBlank()) {
                try {
                    val currentList = json.decodeFromString<List<WatchHistoryItem>>(jsonString).toMutableList()
                    currentList.removeAll { it.id == id && it.mediaType.equals(mediaType, ignoreCase = true) }
                    preferences[PreferencesKeys.WATCH_HISTORY] = json.encodeToString(currentList)
                } catch (_: Exception) {
                }
            }
        }
    }

    suspend fun clearHistory() {
        context.watchHistoryDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.WATCH_HISTORY)
        }
    }
}
