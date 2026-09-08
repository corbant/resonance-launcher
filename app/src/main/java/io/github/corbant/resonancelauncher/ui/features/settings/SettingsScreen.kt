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
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Switch
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

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    // API Configuration Setting
                    ListItem(
                        selected = false,
                        onClick = { viewModel.openConfigModal() },
                        headlineContent = {
                            Text(
                                text = "Configuration & API Keys",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Scan QR code or open local URL for step-by-step API key setup guide",
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(alpha = 0.8f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(
                                    label = "TMDb",
                                    isSet = config.tmdbApiKey.isNotBlank()
                                )
                                StatusBadge(
                                    label = "Streaming",
                                    isSet = config.streamingAvailabilityApiKey.isNotBlank()
                                )
                            }
                        },
                        shape = ListItemDefaults.shape(shape = RoundedCornerShape(12.dp))
                    )

                    // Media Previews Setting
                    ListItem(
                        selected = false,
                        onClick = { viewModel.toggleShowMediaPreviews() },
                        headlineContent = {
                            Text(
                                text = "Show Media Previews",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Display preview videos on the media details page",
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(alpha = 0.8f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = config.showMediaPreviews,
                                onCheckedChange = null
                            )
                        },
                        shape = ListItemDefaults.shape(shape = RoundedCornerShape(12.dp))
                    )

                    // Hidden Apps Management Setting
                    ListItem(
                        selected = false,
                        onClick = { viewModel.openHiddenAppsModal() },
                        headlineContent = {
                            Text(
                                text = "Hidden Apps",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "${uiState.hiddenApps.size} hidden application(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalContentColor.current.copy(alpha = 0.8f)
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        shape = ListItemDefaults.shape(shape = RoundedCornerShape(12.dp))
                    )
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

@Composable
private fun StatusBadge(
    label: String,
    isSet: Boolean,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSet) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        LocalContentColor.current.copy(alpha = 0.7f)
    }
    val backgroundColor = if (isSet) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        LocalContentColor.current.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (isSet) "$label: Set" else "$label: Not Set",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
