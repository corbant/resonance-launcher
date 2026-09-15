package io.github.corbant.resonancelauncher.ui.features.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.OutlinedIconButton
import androidx.tv.material3.OutlinedIconButtonDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.model.MediaDetails
import io.github.corbant.resonancelauncher.model.WatchProvider
import io.github.corbant.resonancelauncher.ui.features.details.components.AmbientTrailerPlayer
import io.github.corbant.resonancelauncher.util.launchAppByPackage
import io.github.corbant.resonancelauncher.util.launchAppStore
import io.github.corbant.resonancelauncher.util.launchTrailer

val VolumeUpIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "VolumeUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(3f, 9f)
            lineTo(3f, 15f)
            lineTo(7f, 15f)
            lineTo(12f, 19f)
            lineTo(12f, 5f)
            lineTo(7f, 9f)
            close()
            moveTo(16.5f, 12f)
            curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
            lineTo(14f, 16.02f)
            curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12f)
            close()
            moveTo(14f, 3.23f)
            lineTo(14f, 5.29f)
            curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
            curveTo(19f, 15.17f, 16.89f, 17.85f, 14f, 18.71f)
            lineTo(14f, 20.77f)
            curveTo(18.01f, 19.86f, 21f, 16.28f, 21f, 12f)
            curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
            close()
        }
    }.build()
}

val VolumeOffIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "VolumeOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(16.5f, 12f)
            curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
            lineTo(14f, 10.18f)
            lineTo(16.45f, 12.63f)
            curveTo(16.48f, 12.43f, 16.5f, 12.22f, 16.5f, 12f)
            close()
            moveTo(19f, 12f)
            curveTo(19f, 12.82f, 18.85f, 13.61f, 18.59f, 14.34f)
            lineTo(20.12f, 15.87f)
            curveTo(20.68f, 14.7f, 21f, 13.39f, 21f, 12f)
            curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
            lineTo(14f, 5.29f)
            curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
            close()
            moveTo(4.27f, 3f)
            lineTo(3f, 4.27f)
            lineTo(7.73f, 9f)
            lineTo(3f, 9f)
            lineTo(3f, 15f)
            lineTo(7f, 15f)
            lineTo(12f, 19f)
            lineTo(12f, 13.27f)
            lineTo(16.25f, 17.52f)
            curveTo(15.58f, 18.04f, 14.83f, 18.45f, 14f, 18.7f)
            lineTo(14f, 20.76f)
            curveTo(15.38f, 20.45f, 16.63f, 19.82f, 17.68f, 18.96f)
            lineTo(19.73f, 21f)
            lineTo(21f, 19.73f)
            lineTo(12f, 10.73f)
            lineTo(4.27f, 3f)
            close()
            moveTo(12f, 5f)
            lineTo(10.12f, 6.5f)
            lineTo(12f, 8.38f)
            lineTo(12f, 5f)
            close()
        }
    }.build()
}

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayProvider: ((WatchProvider?) -> Unit)? = null,
    onTrailerFullScreen: ((String) -> Unit)? = null,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isMuted by remember { mutableStateOf(value = true) }
    var isTrailerPlaying by remember { mutableStateOf(value = false) }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DetailsUiState.Loading -> {
                DetailsBackdrop(backdropUrl = state.initialSummary?.backdropUrl)
                DetailsSkeleton(title = state.initialSummary?.title ?: "")
            }

            is DetailsUiState.Success -> {
                DetailsBackdrop(backdropUrl = state.details.backdropUrl)

                if (state.showMediaPreviews && !state.details.trailerYoutubeKey.isNullOrBlank()) {
                    AmbientTrailerPlayer(
                        youtubeVideoKey = state.details.trailerYoutubeKey,
                        isMuted = isMuted,
                        onIsPlayingChanged = { isTrailerPlaying = it },
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF0F0F12),
                                        Color(0xFF0F0F12).copy(alpha = 0.85f),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 1400f
                                )
                            )
                    )
                }
                DetailsContent(
                    details = state.details,
                    onPlayClicked = { provider ->
                        if (onPlayProvider != null) {
                            onPlayProvider(provider)
                        } else if (provider?.packageName != null) {
                            context.launchAppByPackage(provider.packageName)
                        } else {
                            context.launchAppStore()
                        }
                    },
                    onTrailerClicked = { youtubeKey ->
                        if (onTrailerFullScreen != null) {
                            onTrailerFullScreen(youtubeKey)
                        } else {
                            context.launchTrailer(youtubeKey)
                        }
                    }
                )

                // Top Right Corner Mute Button (Appears only when trailer is actively playing)
                AnimatedVisibility(
                    visible = isTrailerPlaying,
                    enter = fadeIn(tween(600)),
                    exit = fadeOut(tween(600)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 56.dp)
                ) {
                    OutlinedIconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier.size(48.dp),
                        scale = OutlinedIconButtonDefaults.scale(focusedScale = 1.15f),
                        colors = OutlinedIconButtonDefaults.colors(
                            containerColor = Color.Black.copy(alpha = 0.5f),
                            focusedContainerColor = Color.White,
                            contentColor = Color.White,
                            focusedContentColor = Color.Black
                        ),
                        border = OutlinedIconButtonDefaults.border(
                            border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))),
                            focusedBorder = Border(BorderStroke(2.dp, Color.White))
                        )
                    ) {
                        Icon(
                            imageVector = if (isMuted) VolumeOffIcon else VolumeUpIcon,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            is DetailsUiState.Error -> {
                DetailsError(
                    message = state.message,
                    onBack = onBack,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

@Composable
fun DetailsBackdrop(
    backdropUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12))
    ) {
        if (!backdropUrl.isNullOrBlank()) {
            AsyncImage(
                model = backdropUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
            )

            // Horizontal fade from deep canvas color to image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0F0F12),
                                Color(0xFF0F0F12),
                                Color(0xFF0F0F12).copy(alpha = 0.85f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 1400f
                        )
                    )
            )

            // Bottom subtle vignette
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF0F0F12).copy(alpha = 0.9f)
                            ),
                            startY = 400f
                        )
                    )
            )
        }
    }
}

@Composable
fun DetailsSkeleton(
    title: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    val placeholderModifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(Color.White.copy(alpha = alpha))

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.55f)
            .padding(start = 56.dp, top = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Box(
                modifier = placeholderModifier
                    .fillMaxWidth(0.7f)
                    .height(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = placeholderModifier
                    .width(42.dp)
                    .height(20.dp)
            )
            Box(
                modifier = placeholderModifier
                    .width(50.dp)
                    .height(20.dp)
            )
            Box(
                modifier = placeholderModifier
                    .width(64.dp)
                    .height(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = placeholderModifier
                    .fillMaxWidth(0.95f)
                    .height(16.dp)
            )
            Box(
                modifier = placeholderModifier
                    .fillMaxWidth(0.90f)
                    .height(16.dp)
            )
            Box(
                modifier = placeholderModifier
                    .fillMaxWidth(0.60f)
                    .height(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
                    .width(180.dp)
                    .height(48.dp)
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
                    .width(110.dp)
                    .height(48.dp)
            )
        }
    }
}

@Composable
fun DetailsContent(
    details: MediaDetails,
    onPlayClicked: (WatchProvider?) -> Unit,
    onTrailerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playButtonRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            playButtonRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.55f)
            .padding(start = 56.dp, top = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = details.title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (details.rating.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE5A00D))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = details.rating,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            if (details.releaseYear.isNotBlank()) {
                Text(
                    text = details.releaseYear,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            if (details.runtimeOrSeasons.isNotBlank()) {
                Text(text = "•", color = Color.White.copy(alpha = 0.4f))
                Text(
                    text = details.runtimeOrSeasons,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            if (details.genres.isNotEmpty()) {
                Text(text = "•", color = Color.White.copy(alpha = 0.4f))
                Text(
                    text = details.genres.take(2).joinToString(", "),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = details.overview,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Action Buttons Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Watch/Launch Button
            Button(
                onClick = { onPlayClicked(details.watchProviders.firstOrNull()) },
                modifier = Modifier.focusRequester(playButtonRequester),
                shape = ButtonDefaults.shape(CircleShape),
                scale = ButtonDefaults.scale(focusedScale = 1.06f),
                colors = ButtonDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = Color.White,
                    focusedContentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                    Text(
                        text = if (details.watchProviders.isNotEmpty()) {
                            "Watch on ${details.watchProviders.first().name}"
                        } else {
                            "Play"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Trailer Button
            if (!details.trailerYoutubeKey.isNullOrBlank()) {
                OutlinedButton(
                    onClick = { onTrailerClicked(details.trailerYoutubeKey) },
                    shape = ButtonDefaults.shape(CircleShape),
                    scale = ButtonDefaults.scale(focusedScale = 1.06f),
                    border = ButtonDefaults.border(
                        border = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))),
                        focusedBorder = Border(BorderStroke(2.dp, Color.White))
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                        Text("Trailer")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (details.watchProviders.isNotEmpty()) {
            Text(
                text = "Available On",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(details.watchProviders) { provider ->
                    Card(
                        onClick = { onPlayClicked(provider) },
                        modifier = Modifier.size(48.dp),
                        shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
                        scale = CardDefaults.scale(focusedScale = 1.15f),
                        border = CardDefaults.border(
                            focusedBorder = Border(
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            )
                        ),
                        colors = CardDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f))
                    ) {
                        AsyncImage(
                            model = provider.logoUrl,
                            contentDescription = provider.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsError(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val retryButtonRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            retryButtonRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth(0.55f)
            .padding(start = 56.dp, top = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error Loading Details",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRetry,
                modifier = Modifier.focusRequester(retryButtonRequester),
                shape = ButtonDefaults.shape(CircleShape),
                scale = ButtonDefaults.scale(focusedScale = 1.06f)
            ) {
                Text("Retry", fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onBack,
                shape = ButtonDefaults.shape(CircleShape),
                scale = ButtonDefaults.scale(focusedScale = 1.06f)
            ) {
                Text("Go Back")
            }
        }
    }
}
