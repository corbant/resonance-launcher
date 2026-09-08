package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.model.AppItem
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AppContextMenuDialog(
    app: AppItem,
    isFavorite: Boolean,
    favoriteIndex: Int,
    favoriteCount: Int,
    showReorderOptions: Boolean,
    showHideOption: Boolean = false,
    onOpenApp: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onHideApp: () -> Unit = {},
    onUninstallApp: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstButtonRequester = remember { FocusRequester() }
    var isClickable by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150.milliseconds)
        firstButtonRequester.requestFocus()
        delay(100.milliseconds)
        isClickable = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(420.dp)
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
                // App Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = app.iconBitmap,
                            contentDescription = app.label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions List
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Open App Action
                    Button(
                        onClick = {
                            if (isClickable) {
                                onDismiss()
                                onOpenApp()
                            }
                        },
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
                                contentDescription = "Open App",
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Open App")
                        }
                    }

                    // Favorite / Unfavorite Action
                    Button(
                        onClick = {
                            if (isClickable) {
                                onToggleFavorite()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                                modifier = Modifier.size(20.dp),
                                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else Color.White
                            )
                            Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
                        }
                    }

                    // Reorder Actions (only if accessing through favorites list and favorited)
                    if (showReorderOptions && isFavorite && favoriteCount > 1) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (isClickable) {
                                        onMoveLeft()
                                    }
                                },
                                enabled = favoriteIndex > 0,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Move Left",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text("Move Left")
                                }
                            }

                            Button(
                                onClick = {
                                    if (isClickable) {
                                        onMoveRight()
                                    }
                                },
                                enabled = favoriteIndex < favoriteCount - 1,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Move Right")
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Move Right",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Hide App Action (when triggered from all apps modal)
                    if (showHideOption) {
                        Button(
                            onClick = {
                                if (isClickable) {
                                    onDismiss()
                                    onHideApp()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Hide App",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Hide App")
                            }
                        }
                    }

                    // Uninstall Action
                    Button(
                        onClick = {
                            if (isClickable) {
                                onDismiss()
                                onUninstallApp()
                            }
                        },
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
                                contentDescription = "Uninstall App",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text("Uninstall App", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}