package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.model.AppItem

@Composable
fun CircularAppIcon(
    app: AppItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    showLabel: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textScale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "textScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1.0f else 0.7f,
        animationSpec = tween(durationMillis = 200),
        label = "textAlpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            shape = CardDefaults.shape(shape = CircleShape),
            border = CardDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(2.dp, Color.White),
                    shape = CircleShape,
                    inset = 4.dp
                ),
                pressedBorder = Border(
                    border = BorderStroke(2.dp, Color.White),
                    shape = CircleShape,
                    inset = 3.dp
                )
            ),
            glow = CardDefaults.glow(
                focusedGlow = Glow(elevationColor = Color(app.dominantColor), elevation = 20.dp),
                pressedGlow = Glow(elevationColor = Color(app.dominantColor), elevation = 12.dp)
            ),
            interactionSource = interactionSource,
            modifier = modifier
                .width(72.dp)
                .aspectRatio(CardDefaults.SquareImageAspectRatio),
            onClick = onClick,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    model = app.iconBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = textAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = textScale
                        scaleY = textScale
                        transformOrigin = TransformOrigin(0.5f, 0.0f)
                    }
            )
        }
    }
}