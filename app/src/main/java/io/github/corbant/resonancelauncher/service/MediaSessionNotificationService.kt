package io.github.corbant.resonancelauncher.service

import android.app.Notification
import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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
    private val notificationTitles = ConcurrentHashMap<String, String>()

    override fun onCreate() {
        super.onCreate()
        setupMediaSessionObserver()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            activeNotifications.forEach(::updateNotificationTitle)
            updateActiveSessionsForPackages(notificationTitles.keys)
        } catch (_: Exception) {
        }
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
        notificationTitles.clear()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        updateNotificationTitle(sbn)
        updateActiveSessionsForPackages(listOf(sbn.packageName))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        notificationTitles.remove(packageName)
        try {
            activeNotifications
                .filter { it.packageName == packageName }
                .forEach(::updateNotificationTitle)
        } catch (_: Exception) {
        }
    }

    private fun updateNotificationTitle(sbn: StatusBarNotification) {
        val notification = sbn.notification
        val extras = notification.extras
        val hasMediaSession = extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true
        val isMediaNotification = hasMediaSession ||
                notification.category == Notification.CATEGORY_TRANSPORT
        if (!isMediaNotification) return

        val title = sequenceOf(
            extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        ).firstOrNull { !it.isNullOrBlank() }?.trim()

        if (title == null) {
            notificationTitles.remove(sbn.packageName)
        } else {
            notificationTitles[sbn.packageName] = title
        }
    }

    private fun updateActiveSessionsForPackages(packageNames: Collection<String>) {
        if (packageNames.isEmpty()) return
        activeCallbacks.keys
            .filter { it.packageName in packageNames }
            .forEach { controller ->
                handleStateOrMetadataUpdate(
                    controller,
                    controller.playbackState,
                    controller.metadata
                )
            }
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
                    handleStateOrMetadataUpdate(
                        controller,
                        controller.playbackState,
                        controller.metadata
                    )
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
        if (state == null) return

        val title = sequenceOf(
            metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
            metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
            notificationTitles[controller.packageName]
        ).firstOrNull { !it.isNullOrBlank() }?.trim() ?: return

        val durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        if (durationMs <= 0) return

        val currentPosMs =
            if (state.state == PlaybackState.STATE_PLAYING && state.lastPositionUpdateTime > 0) {
                val elapsedSinceUpdate =
                    SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
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
        val isStoppedState =
            state.state == PlaybackState.STATE_STOPPED || state.state == PlaybackState.STATE_NONE
        val isNearEnd = remainingMs in 1..30_000 || progressRatio >= 0.92f
        val isCompleted =
            (isStoppedState && progressRatio >= 0.50f) || isNearEnd || isTitleChangedInSameSession

        val packageName = controller.packageName
        val appName = try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }

        val albumOrSeries = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)

        val artUri = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)

        val isTvType =
            albumOrSeries != null || title.contains("Episode", ignoreCase = true) || title.contains(
                "Season",
                ignoreCase = true
            )
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
