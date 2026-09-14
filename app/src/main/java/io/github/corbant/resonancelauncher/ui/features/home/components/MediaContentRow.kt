package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import io.github.corbant.resonancelauncher.model.MediaItem

@Composable
fun MediaContentRow(
    title: String,
    items: List<MediaItem>,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
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
                cardWidth = 150.dp,
                aspectRatio = 2f / 3f,
                modifier = if (index == 0) Modifier.focusRequester(fallbackFocusRequester) else Modifier,
                onFocused = {},
                onUnfocused = {},
                onClick = { onMediaClick(item) }
            )
        }
    }
}