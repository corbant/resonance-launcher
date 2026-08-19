package io.github.corbant.resonancelauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.StandardCardContainer
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.data.TvAppItem

@Composable
fun AppGrid(
    apps: List<TvAppItem>,
    onAppClick: (TvAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(apps, key = { it.packageName }) { app ->
            TvAppCard(app = app, onClick = { onAppClick(app) })
        }
    }
}

@Composable
fun TvAppCard(
    app: TvAppItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    StandardCardContainer(
        imageCard = {
            Card(
                colors = CardDefaults.compactCardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                border = CardDefaults.border(
                    focusedBorder = Border(
                        border = BorderStroke(3.dp, Color.White)
                    )
                ),
                scale = CardDefaults.scale(
                    focusedScale = 1.1f
                ),
                onClick = onClick,
                interactionSource = interactionSource
            ) {
                AsyncImage(
                    model = app.image,
                    contentDescription = app.label,
                    contentScale = if (app.isBanner) ContentScale.Crop else ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (app.isBanner) 0.dp else 20.dp)
                )
            }
        }, title = {
            AnimatedVisibility(
                visible = isFocused,
                enter = fadeIn(),
                exit = fadeOut()

            ) {
                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    )
}