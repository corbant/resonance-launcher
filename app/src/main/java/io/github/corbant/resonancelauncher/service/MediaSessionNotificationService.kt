package io.github.corbant.resonancelauncher.service

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import io.github.corbant.resonancelauncher.data.repository.WatchHistoryRepository
import io.github.corbant.resonancelauncher.model.WatchHistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MediaSessionNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val watchHistoryRepository by lazy { WatchHistoryRepository(applicationContext) }
    private var mediaSessionManager: MediaSessionManager? = null

    private val activeCallbacks = ConcurrentHashMap<MediaController, MediaController.Callback>()
    private val lastSessionTitles = ConcurrentHashMap<MediaController, String>()

    override fun onCreate() {
        super.onCreate()
        setupMediaSessionObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeCallbacks.forEach { (controller, callback) ->
            try {
                controller.unregisterCallback(callback)
            } catch (_: Exception) {
            }
        }
        activeCallbacks.clear()
        lastSessionTitles.clear()
        serviceScope.cancel()
    }

    private fun setupMediaSessionObserver() {
        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as? MediaSessionManager
        val componentName = ComponentName(this, MediaSessionNotificationService::class.java)

        try {
            val controllers = mediaSessionManager?.getActiveSessions(componentName)
            registerControllers(controllers)

            mediaSessionManager?.addOnActiveSessionsChangedListener({ newControllers ->
                registerControllers(newControllers)
            }, componentName)
        } catch (_: Exception) {
        }
    }

    private fun registerControllers(controllers: List<MediaController>?) {
        if (controllers == null) return
        controllers.forEach { controller ->
            if (!activeCallbacks.containsKey(controller)) {
                val callback = object : MediaController.Callback() {
                    override fun onPlaybackStateChanged(state: PlaybackState?) {
                        handleStateOrMetadataUpdate(controller, state, controller.metadata)
                    }

                    override fun onMetadataChanged(metadata: MediaMetadata?) {
                        handleStateOrMetadataUpdate(controller, controller.playbackState, metadata)
                    }

                    override fun onSessionDestroyed() {
                        activeCallbacks.remove(controller)
                        lastSessionTitles.remove(controller)
                    }
                }
                try {
                    controller.registerCallback(callback)
                    activeCallbacks[controller] = callback
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun handleStateOrMetadataUpdate(
        controller: MediaController,
        state: PlaybackState?,
        metadata: MediaMetadata?
    ) {
        if (state == null || metadata == null) return

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: return

        val durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
        if (durationMs <= 0) return

        val currentPosMs = if (state.state == PlaybackState.STATE_PLAYING && state.lastPositionUpdateTime > 0) {
            val elapsedSinceUpdate = SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
            (state.position + (elapsedSinceUpdate * state.playbackSpeed)).toLong()
        } else {
            state.position
        }

        val remainingMs = durationMs - currentPosMs
        val progressRatio = (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

        // Check if title changed within the same controller session (e.g. next episode auto-played)
        val previousTitle = lastSessionTitles[controller]
        val isTitleChangedInSameSession = previousTitle != null && previousTitle != title
        lastSessionTitles[controller] = title

        // Multi-signal Completion criteria
        val isStoppedState = state.state == PlaybackState.STATE_STOPPED || state.state == PlaybackState.STATE_NONE
        val isNearEnd = remainingMs in 1..30_000 || progressRatio >= 0.92f
        val isCompleted = (isStoppedState && progressRatio >= 0.50f) || isNearEnd || isTitleChangedInSameSession

        val packageName = controller.packageName
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }

        val albumOrSeries = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)

        val artUri = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)

        val isTvType = albumOrSeries != null || title.contains("Episode", ignoreCase = true) || title.contains("Season", ignoreCase = true)
        val mediaType = if (isTvType) "tv" else "movie"

        val itemId = (title + packageName).hashCode()

        serviceScope.launch {
            if (isCompleted) {
                // Remove finished item from history
                watchHistoryRepository.removeFromHistory(itemId, mediaType)
                if (isTitleChangedInSameSession) {
                    // Record new auto-played item
                    val newWatchItem = WatchHistoryItem(
                        id = itemId,
                        title = title,
                        mediaType = mediaType,
                        posterUrl = artUri,
                        lastWatchedTimestamp = System.currentTimeMillis(),
                        providerPackageName = packageName,
                        providerName = appName,
                        episodeTitle = title,
                        progressPercentage = 0.05f
                    )
                    watchHistoryRepository.recordWatch(newWatchItem)
                }
            } else if (state.state == PlaybackState.STATE_PAUSED && progressRatio in 0.03f..0.92f) {
                // Record / update pause position
                val watchItem = WatchHistoryItem(
                    id = itemId,
                    title = title,
                    mediaType = mediaType,
                    posterUrl = artUri,
                    lastWatchedTimestamp = System.currentTimeMillis(),
                    providerPackageName = packageName,
                    providerName = appName,
                    episodeTitle = if (albumOrSeries != null) title else null,
                    progressPercentage = progressRatio
                )
                watchHistoryRepository.recordWatch(watchItem)
            }
        }
    }
}
