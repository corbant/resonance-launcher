package io.github.corbant.resonancelauncher.ui.features.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import io.github.corbant.resonancelauncher.model.MediaItem
import io.github.corbant.resonancelauncher.ui.features.home.components.AppGrid
import io.github.corbant.resonancelauncher.ui.features.home.components.AppTrayRow
import io.github.corbant.resonancelauncher.ui.features.home.components.ContinueWatchingRow
import io.github.corbant.resonancelauncher.ui.features.home.components.HomeTopBar
import io.github.corbant.resonancelauncher.ui.features.home.components.MediaContentRow
import io.github.corbant.resonancelauncher.util.launchAppByPackage
import io.github.corbant.resonancelauncher.util.launchAppStore
import io.github.corbant.resonancelauncher.util.launchSystemSettings

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onMediaClick: (id: Int, type: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAllAppsOverlay by rememberSaveable { mutableStateOf(false) }

    val installedApps = (uiState as? HomeUiState.Success)?.installedApps ?: emptyList()

    BackHandler(enabled = true) {
        showAllAppsOverlay = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            HomeTopBar(
                onOpenLauncherSettings = onNavigateToSettings,
                onOpenSystemSettings = { context.launchSystemSettings() },
            )

            HomeContent(
                uiState = uiState,
                onMediaFocused = viewModel::onMediaFocused,
                onMediaClick = onMediaClick,
                onLaunchApp = { packageName -> context.launchAppByPackage(packageName) },
                onShowAllApps = { showAllAppsOverlay = true },
                modifier = modifier,
            )
        }

        AnimatedVisibility(
            visible = showAllAppsOverlay,
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 64.dp, vertical = 40.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .focusGroup(),
                    shape = RoundedCornerShape(32.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 48.dp, vertical = 36.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "All Apps",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "${installedApps.size} applications installed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Button(onClick = {
                                showAllAppsOverlay = false
                                context.launchAppStore()
                            }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.AddCircle,
                                        contentDescription = "App Store",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(text = "App Store")
                                }

                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        AppGrid(
                            apps = installedApps,
                            onLaunchApp = { packageName ->
                                showAllAppsOverlay = false
                                context.launchAppByPackage(packageName)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onMediaFocused: (MediaItem) -> Unit,
    onMediaClick: (Int, String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onShowAllApps: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading catalog...", color = Color.White)
                }
            }

            is HomeUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                }
            }

            is HomeUiState.Success -> {
                Crossfade(
                    targetState = uiState.featuredBackdropUrl,
                    animationSpec = tween(600),
                    label = "BackdropCrossfade"
                ) { backdropUrl ->
                    if (backdropUrl != null) {
                        AsyncImage(
                            model = backdropUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black
                                )
                            )
                        )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRestorer(),
                    contentPadding = PaddingValues(bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    items(uiState.contentSections) { section ->
                        when (section) {
                            is HomeSection.AppTray -> {
                                AppTrayRow(
                                    apps = section.apps,
                                    title = section.title,
                                    onLaunchApp = onLaunchApp,
                                    onShowAllApps = onShowAllApps
                                )
                            }

                            is HomeSection.MediaContent -> {
                                MediaContentRow()
                            }

                            is HomeSection.ContinueWatching -> {
                                ContinueWatchingRow()
                            }
                        }
                    }
                }
            }
        }
    }
}