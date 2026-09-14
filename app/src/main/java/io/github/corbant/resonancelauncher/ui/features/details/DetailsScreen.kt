package io.github.corbant.resonancelauncher.ui.features.details

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.model.MediaDetails
import io.github.corbant.resonancelauncher.model.WatchProvider
import io.github.corbant.resonancelauncher.ui.features.details.components.AmbientTrailerPlayer
import io.github.corbant.resonancelauncher.util.launchAppByPackage
import io.github.corbant.resonancelauncher.util.launchAppStore
import io.github.corbant.resonancelauncher.util.launchTrailer

@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayProvider: ((WatchProvider?) -> Unit)? = null,
    onTrailerFullScreen: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isMuted by remember { mutableStateOf(value = true) }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is DetailsUiState.Loading -> {
                DetailsBackdrop(backdropUrl = state.initialSummary?.backdropUrl)
                DetailsSkeleton(title = state.initialSummary?.title ?: "")
            }

            is DetailsUiState.Success -> {
                DetailsBackdrop(backdropUrl = state.details.backdropUrl)

                if (!state.details.trailerYoutubeKey.isNullOrBlank()) {
                    AmbientTrailerPlayer(
                        youtubeVideoKey = state.details.trailerYoutubeKey,
                        isMuted = isMuted,
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
        // Real title if known from summary, otherwise placeholder
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

        // Metadata Pills Skeleton (Year • Rating • Runtime)
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

        // Overview Skeleton (3 Lines)
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

        // Action Buttons Skeleton
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
        // Title
        Text(
            text = details.title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Metadata: Rating • Year • Runtime • Genres
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

        // Overview
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

        // Watch Providers Row
        if (details.watchProviders.isNotEmpty()) {
            Text(
                text = "Available On",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onRetry,
                modifier = Modifier.focusRequester(retryButtonRequester),
                shape = ButtonDefaults.shape(CircleShape)
            ) {
                Text("Retry")
            }
            OutlinedButton(
                onClick = onBack,
                shape = ButtonDefaults.shape(CircleShape)
            ) {
                Text("Back")
            }
        }
    }
}
