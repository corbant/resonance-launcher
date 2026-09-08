package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import io.github.corbant.resonancelauncher.model.AppItem

@Composable
fun AppTrayRow(
    apps: List<AppItem>,
    title: String,
    onLaunchApp: (String) -> Unit,
    onAppLongClick: (AppItem) -> Unit,
    onShowAllApps: () -> Unit
) {
    HomeRow(
        title = title
    ) { fallbackFocusRequester ->
        itemsIndexed(
            apps,
            key = { _, it -> it.packageName }) { index, app ->
            CircularAppIcon(
                app = app,
                modifier = if (index == 0) Modifier.focusRequester(
                    fallbackFocusRequester
                ) else Modifier,
                onClick = { onLaunchApp(app.packageName) },
                onLongClick = { onAppLongClick(app) },
                showLabel = false
            )
        }
        item(key = "action_all_apps") {
            IconButton(onClick = onShowAllApps) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "All Apps"
                )
            }
        }
    }
}