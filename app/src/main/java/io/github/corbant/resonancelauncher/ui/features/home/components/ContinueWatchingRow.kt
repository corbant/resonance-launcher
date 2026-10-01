package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.model.WatchHistoryItem

@Composable
fun ContinueWatchingRow(
    items: List<WatchHistoryItem>,
    onItemClick: (WatchHistoryItem) -> Unit,
    onRemoveItem: (WatchHistoryItem) -> Unit,
    onOpenDetails: (WatchHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Continue Watching"
) {
    if (items.isEmpty()) return

    var selectedContextItem by remember { mutableStateOf<WatchHistoryItem?>(null) }

    HomeRow(
        title = title,
        modifier = modifier
    ) { fallbackFocusRequester ->
        itemsIndexed(
            items = items,
            key = { _, item -> "${item.id}_${item.mediaType}" }
        ) { index, item ->
            ContinueWatchingCard(
                item = item,
                onClick = { onItemClick(item) },
                onLongClick = { selectedContextItem = item },
                modifier = if (index == 0) Modifier.focusRequester(fallbackFocusRequester) else Modifier
            )
        }
    }

    if (selectedContextItem != null) {
        val currentItem = selectedContextItem!!
        ContinueWatchingContextMenuDialog(
            item = currentItem,
            onResume = {
                selectedContextItem = null
                onItemClick(currentItem)
            },
            onOpenDetails = {
                selectedContextItem = null
                onOpenDetails(currentItem)
            },
            onRemove = {
                selectedContextItem = null
                onRemoveItem(currentItem)
            },
            onDismiss = { selectedContextItem = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContinueWatchingCard(
    item: WatchHistoryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Dp = 220.dp,
    aspectRatio: Float = 16f / 9f
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.width(cardWidth)
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .onFocusChanged { isFocused = it.isFocused }
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = CardDefaults.shape(shape = RoundedCornerShape(16.dp)),
            scale = CardDefaults.scale(focusedScale = 1.08f),
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(2.dp, Color.White),
                    shape = RoundedCornerShape(16.dp)
                )
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Artwork
                AsyncImage(
                    model = item.backdropUrl ?: item.posterUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.85f)
                                ),
                                startY = 60f
                            )
                        )
                )

                // Streaming Provider Badge (Top-Right)
                if (!item.providerName.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.providerName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Title overlay
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                )

                // Progress Bar Overlay
                val progress = (item.progressPercentage ?: 0.15f).coerceIn(0.05f, 1.0f)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Episode / Media Subtitle below card
        val subtitleText = when {
            item.mediaType.equals("tv", ignoreCase = true) -> {
                val s = item.seasonNumber ?: 1
                val e = item.episodeNumber ?: 1
                if (!item.nextEpisodeTitle.isNullOrBlank()) {
                    "Next: ${item.nextEpisodeTitle}"
                } else if (!item.episodeTitle.isNullOrBlank()) {
                    "S$s:E$e • ${item.episodeTitle}"
                } else {
                    "S$s:E$e"
                }
            }
            else -> "Movie"
        }

        Text(
            text = subtitleText,
            style = MaterialTheme.typography.labelMedium,
            color = if (isFocused) Color.White else Color.White.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun ContinueWatchingContextMenuDialog(
    item: WatchHistoryItem,
    onResume: () -> Unit,
    onOpenDetails: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstButtonRequester = remember { FocusRequester() }

    BackHandler(onBack = onDismiss)

    LaunchedEffect(Unit) {
        firstButtonRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(24.dp)
                )
                .focusGroup()
                .focusRestorer(fallback = firstButtonRequester),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (!item.posterUrl.isNullOrBlank() || !item.backdropUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = item.posterUrl ?: item.backdropUrl,
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (item.providerName.isNullOrBlank()) "Media Details" else "On ${item.providerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Resume Action
                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(firstButtonRequester)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Resume Watching",
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Resume Watching")
                        }
                    }

                    // View Details Action
                    Button(
                        onClick = onOpenDetails,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "View Details",
                                modifier = Modifier.size(20.dp)
                            )
                            Text("View Details")
                        }
                    }

                    // Remove from Continue Watching Action
                    Button(
                        onClick = onRemove,
                        colors = ButtonDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Remove from Continue Watching",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text("Remove from Continue Watching", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
