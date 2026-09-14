@file:Suppress("DEPRECATION")

package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.corbant.resonancelauncher.model.MediaItem
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImmersiveMediaContentRow(
    title: String,
    items: List<MediaItem>,
    onMediaFocused: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    onMediaUnfocused: (MediaItem) -> Unit = {},
) {
    val screenHeight = LocalWindowInfo.current.containerDpSize.height
    val collapsedHeight = 300.dp

    var isFocused by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<MediaItem?>(null) }
    var rowSize by remember { mutableStateOf(Size.Zero) }

    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    val animatedHeight by animateDpAsState(
        targetValue = if (isFocused) screenHeight else collapsedHeight,
        animationSpec = tween(durationMillis = 350),
        label = "ImmersiveHeightAnim",
        finishedListener = { _ ->
            if (isFocused) {
                coroutineScope.launch {
                    bringIntoViewRequester.bringIntoView()
                }
            }
        }
    )

    LaunchedEffect(isFocused) {
        if (isFocused) {
            snapshotFlow { animatedHeight }.collect {
                bringIntoViewRequester.bringIntoView()
            }
        }
    }

    val bringIntoViewResponder = remember {
        object : BringIntoViewResponder {
            override fun calculateRectForParent(localRect: Rect): Rect {
                return Rect(
                    left = localRect.left,
                    top = 0f,
                    right = localRect.right,
                    bottom = rowSize.height.coerceAtLeast(localRect.bottom)
                )
            }

            override suspend fun bringChildIntoView(localRect: () -> Rect?) {
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .onSizeChanged { rowSize = it.toSize() }
            .bringIntoViewResponder(bringIntoViewResponder)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusChanged { state ->
                isFocused = state.hasFocus
            }
    ) {
        AnimatedVisibility(
            visible = isFocused,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(150)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 56.dp, top = 80.dp)
                .fillMaxWidth(0.5f)
        ) {
            AnimatedContent(
                targetState = selectedItem,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { height -> height / 4 })
                        .togetherWith(fadeOut(tween(150)))
                },
                label = "ImmersiveMediaTextTransition"
            ) { item ->
                Column {
                    Text(
                        text = item?.title.orEmpty(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    if (!item?.rating.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = item.rating,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item?.overview.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        HomeRow(
            title = if (!isFocused) title else null,
            modifier = Modifier.align(Alignment.BottomStart)
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
                    onFocused = {
                        selectedItem = item
                        onMediaFocused(item)
                    },
                    onUnfocused = { onMediaUnfocused(item) },
                    onClick = { onMediaClick(item) }
                )
            }
        }
    }
}
