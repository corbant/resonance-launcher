package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.corbant.resonancelauncher.model.MediaItem

@Composable
fun MediaContentRow(
    title: String,
    items: List<MediaItem>,
    onMediaFocused: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    onMediaUnfocused: (MediaItem) -> Unit = {},
    cardWidth: Dp = 150.dp,
    aspectRatio: Float = 2f / 3f,
) {
    if (items.isEmpty()) return

    HomeRow(
        title = title,
        modifier = modifier,
    ) { fallbackFocusRequester ->
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            MediaPosterCard(
                item = item,
                cardWidth = cardWidth,
                aspectRatio = aspectRatio,
                modifier = if (index == 0) Modifier.focusRequester(fallbackFocusRequester) else Modifier,
                onFocused = { onMediaFocused(item) },
                onUnfocused = { onMediaUnfocused(item) },
                onClick = { onMediaClick(item) }
            )
        }
    }
}