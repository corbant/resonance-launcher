package io.github.corbant.resonancelauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.corbant.resonancelauncher.data.AppItem

@Composable
fun AppGrid(
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppCard(app = app, onClick = { onAppClick(app) })
        }
    }
}