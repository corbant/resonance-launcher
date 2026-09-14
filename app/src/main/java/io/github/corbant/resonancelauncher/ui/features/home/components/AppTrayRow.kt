package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
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
            IconButton(onClick = onShowAllApps, modifier = Modifier.size(72.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = "All Apps",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}