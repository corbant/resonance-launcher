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
) {
    var isVideoReady by remember { mutableStateOf(value = false) }
    var activePlayer by remember { mutableStateOf<YouTubePlayer?>(null) }
    var playerState by remember { mutableStateOf<PlayerConstants.PlayerState?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val alpha by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(1200),
        label = "trailer_fade",
    )

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
                        val field = IFramePlayerOptions.Builder::class.java.getDeclaredField("builderOptions")
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
                                    function applyStyles(doc) {
                                        if (!doc) return;
                                        try {
                                            var style = doc.getElementById('resonance-custom-styles');
                                            if (!style) {
                                                style = doc.createElement('style');
                                                style.id = 'resonance-custom-styles';
                                                style.innerHTML = 'html, body, iframe, #player, .html5-video-player, .html5-video-container { pointer-events: none !important; user-select: none !important; } .ytp-bezel, .ytp-bezel-icon, .ytp-bezel-text, .ytp-large-play-button, .ytp-large-play-button-red-bg, .ytp-button, .ytp-play-button, .ytp-chrome-top, .ytp-chrome-bottom, .ytp-gradient-top, .ytp-gradient-bottom, .ytp-watermark, .ytp-pause-overlay, .ytp-scroll-min, .ytp-pause-overlay-container, .ytp-ce-element, .caption-window, .ytp-caption-window-container, .ytp-caption-segment, .ytp-spinner { display: none !important; opacity: 0 !important; visibility: hidden !important; width: 0px !important; height: 0px !important; pointer-events: none !important; }';
                                                (doc.head || doc.documentElement).appendChild(style);
                                            }
                                        } catch(e) {}
                                    }
                                    try {
                                        if (window.player) {
                                            if (window.player.unloadModule) {
                                                window.player.unloadModule('captions');
                                                window.player.unloadModule('cc');
                                            }
                                            if (window.player.setOption) {
                                                window.player.setOption('captions', 'track', {});
                                                window.player.setOption('cc', 'track', {});
                                            }
                                        }
                                    } catch (e) {}
                                    applyStyles(document);
                                    try {
                                        for (var i = 0; i < window.frames.length; i++) {
                                            try {
                                                applyStyles(window.frames[i].document);
                                            } catch(e) {}
                                        }
                                    } catch(e) {}
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
