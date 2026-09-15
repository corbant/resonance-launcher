package io.github.corbant.resonancelauncher.ui.features.details.components

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AmbientTrailerPlayer(
    youtubeVideoKey: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
    onIsPlayingChanged: (Boolean) -> Unit = {},
) {
    var isVideoReady by remember { mutableStateOf(value = false) }
    var activePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var playerState by remember { mutableStateOf<PlayerConstants.PlayerState?>(null) }
    var videoDuration by remember { mutableFloatStateOf(value = 0f) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val CSS_OVERLAY_HIDE = """
        /* All End Cards, Suggested Videos & Overlays */
        .ytp-endscreen-paginate,
        .ytp-endscreen-content,
        .html5-endscreen,
        .ytp-ce-element,
        .ytp-ce-element-show,
        .ytp-ce-covering-overlay,
        .ytp-ce-covering-image,
        .ytp-ce-expanding-overlay,
        .ytp-ce-channel,
        .ytp-ce-video,
        .ytp-pause-overlay,
        .ytp-pause-overlay-container,
        .ytp-scroll-min,
        
        /* Top Chrome & Branding */
        .ytp-chrome-top,
        .ytp-title,
        .ytp-title-text,
        .ytp-title-channel,
        .ytp-watermark,
        .ytp-show-cards-title,
        .ytp-gradient-top,
        .ytp-gradient-bottom,
        
        /* Captions / Subtitles */
        .ytp-caption-window-container,
        .caption-window,
        .ytp-subtitles-player-srv,
        .ytp-caption-segment {
            display: none !important;
            opacity: 0 !important;
            visibility: hidden !important;
            pointer-events: none !important;
            width: 0 !important;
            height: 0 !important;
            max-width: 0 !important;
            max-height: 0 !important;
            transform: translateY(-99999px) !important;
        }
    """.trimIndent().replace("\n", " ")

    val alpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(1200),
        label = "trailer_fade",
    )

    // Notify parent when active video playback state changes
    LaunchedEffect(isVideoReady) {
        onIsPlayingChanged(isVideoReady)
    }

    // Dynamically toggle mute state on the running player
    LaunchedEffect(isMuted) {
        if (isMuted) activePlayer?.mute() else activePlayer?.unMute()
    }

    // Delay setting isVideoReady until 4 seconds after steady playback has started.
    // This guarantees YouTube's initial play icon animation and title card have fully finished while alpha is 0.
    LaunchedEffect(playerState) {
        when (playerState) {
            PlayerConstants.PlayerState.PLAYING -> {
                if (!isVideoReady) {
                    kotlinx.coroutines.delay(4000.milliseconds)
                    isVideoReady = true
                }
            }

            PlayerConstants.PlayerState.PAUSED,
            PlayerConstants.PlayerState.ENDED -> {
                isVideoReady = false
            }

            else -> Unit
        }
    }

    // Handle lifecycle events cleanly
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    isVideoReady = false
                    activePlayer?.pause()
                }

                Lifecycle.Event.ON_RESUME -> activePlayer?.play()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
        }
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                YouTubePlayerView(context).apply {
                    setBackgroundColor(Color.TRANSPARENT)
                    isFocusable = false
                    isFocusableInTouchMode = false
                    isClickable = false
                    isEnabled = false
                    enableAutomaticInitialization = false

                    val builder = IFramePlayerOptions.Builder(context)
                        .controls(0)
                        .ccLoadPolicy(0)
                        .ivLoadPolicy(3)
                        .rel(0)

                    try {
                        val field =
                            IFramePlayerOptions.Builder::class.java.getDeclaredField("builderOptions")
                        field.isAccessible = true
                        val json = field[builder] as? JSONObject
                        json?.apply {
                            put("autohide", 1)
                            put("modestbranding", 1)
                            put("disablekb", 1)
                            put("showinfo", 0)
                            put("playsinline", 1)
                        }
                    } catch (_: Exception) {
                    }

                    val options = builder.build()

                    val setupWebView = {
                        findWebView()?.apply {
                            setBackgroundColor(Color.TRANSPARENT)
                            isFocusable = false
                            isFocusableInTouchMode = false
                            isClickable = false
                            isEnabled = false
                            @Suppress("ClickableViewAccessibility")
                            setOnTouchListener { _, _ -> true }

                            evaluateJavascript(
                                """
                                (function() {
                                    var styleId = 'yt-remove-style';
                                    var existing = document.getElementById(styleId);
                                    if (!existing) {
                                        var style = document.createElement('style');
                                        style.type = 'text/css';
                                        style.innerHTML = '$CSS_OVERLAY_HIDE';
                                        (document.head || document.documentElement).appendChild(style);
                                    }
                                    
                                    try {
                                        var player = window.player || document.getElementById('player');
                                        if (player) {
                                            if (typeof player.unloadModule === 'function') {
                                                player.unloadModule('captions');
                                                player.unloadModule('cc');
                                            }
                                            if (typeof player.setOptions === 'function') {
                                                player.setOption('captions', 'track', {});
                                                player.setOption('cc', 'track', {});
                                            }
                                        }
                                    } catch (e) {}
                                })();
                                """.trimIndent(),
                                null
                            )
                        }
                    }

                    val listener = object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            activePlayer = youTubePlayer
                            setupWebView()
                            if (isMuted) youTubePlayer.mute() else youTubePlayer.unMute()
                            youTubePlayer.loadVideo(youtubeVideoKey, 0f)
                        }

                        override fun onStateChange(
                            youTubePlayer: YouTubePlayer,
                            state: PlayerConstants.PlayerState
                        ) {
                            setupWebView()
                            playerState = state
                        }

                        override fun onVideoDuration(
                            youTubePlayer: YouTubePlayer,
                            duration: Float
                        ) {
                            videoDuration = duration
                        }

                        override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                            // Fade out 2 seconds before the video ends to prevent showing YouTube end cards or cover pages
                            if (videoDuration > 0f && (videoDuration - second) <= 2f && isVideoReady) {
                                isVideoReady = false
                            }
                        }
                    }

                    initialize(listener, options)
                }
            },
            onRelease = { playerView ->
                playerView.release()
            }
        )
    }
}

private fun View.findWebView(): WebView? {
    if (this is WebView) return this
    if (this is ViewGroup) {
        for (i in 0 until childCount) {
            val child = getChildAt(i).findWebView()
            if (child != null) return child
        }
    }
    return null
}
