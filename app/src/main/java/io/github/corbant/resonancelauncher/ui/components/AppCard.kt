package io.github.corbant.resonancelauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Glow
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.data.AppItem

@Composable
fun AppCard(
    app: AppItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
            focusedGlow = Glow(elevationColor = Color(app.dominantColor), elevation = 28.dp),
            pressedGlow = Glow(elevationColor = Color(app.dominantColor), elevation = 14.dp)
        ),
        modifier = modifier
            .width(80.dp)
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
}