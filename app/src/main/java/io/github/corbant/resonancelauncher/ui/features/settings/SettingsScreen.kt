package io.github.corbant.resonancelauncher.ui.features.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.corbant.resonancelauncher.ui.features.settings.components.ConfigModal
import io.github.corbant.resonancelauncher.ui.features.settings.components.HiddenAppsModal

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val config = uiState.config
    val isModalOpen = uiState.pairingModal !is PairingModalState.Idle

    BackHandler(enabled = isModalOpen || uiState.isHiddenAppsModalOpen) {
        if (uiState.isHiddenAppsModalOpen) {
            viewModel.closeHiddenAppsModal()
        } else if (isModalOpen) {
            viewModel.closeConfigModal()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 64.dp, vertical = 40.dp)
            ) {
                Text(
                    text = "Launcher Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(0.65f)
                ) {
                    // API Configuration Card
                    Card(
                        onClick = { viewModel.openConfigModal() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Configuration & API Keys",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Scan QR code or open local URL for step-by-step API key setup guide",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = if (config.tmdbApiKey.isNotBlank()) "TMDb: Set" else "TMDb: Not Set",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (config.tmdbApiKey.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                    Text(
                                        text = if (config.streamingAvailabilityApiKey.isNotBlank()) "Streaming: Set" else "Streaming: Not Set",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (config.streamingAvailabilityApiKey.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Media Previews Toggle Card
                    Card(
                        onClick = { viewModel.toggleShowMediaPreviews() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Show Media Previews",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Display backdrop artwork on the home screen when focused on media content",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (config.showMediaPreviews) MaterialTheme.colorScheme.primaryContainer
                                        else Color.White.copy(alpha = 0.12f)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (config.showMediaPreviews) "ON" else "OFF",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (config.showMediaPreviews) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                )
                            }
                        }
                    }

                    // Hidden Apps Management Card
                    Card(
                        onClick = { viewModel.openHiddenAppsModal() },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hidden Apps",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = "${uiState.hiddenApps.size} hidden application(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.isHiddenAppsModalOpen,
            enter = fadeIn() + scaleIn(initialScale = 0.94f),
            exit = fadeOut() + scaleOut(targetScale = 0.94f)
        ) {
            HiddenAppsModal(
                hiddenApps = uiState.hiddenApps,
                onUnhideApp = { packageName -> viewModel.unhideApp(packageName) },
                onDismiss = { viewModel.closeHiddenAppsModal() }
            )
        }

        AnimatedVisibility(
            visible = isModalOpen,
            enter = fadeIn() + scaleIn(initialScale = 0.94f),
            exit = fadeOut() + scaleOut(targetScale = 0.94f)
        ) {
            ConfigModal(
                modalState = uiState.pairingModal,
                onDismiss = { viewModel.closeConfigModal() }
            )
        }
    }
}