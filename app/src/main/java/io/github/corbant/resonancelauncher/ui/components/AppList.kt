package io.github.corbant.resonancelauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.corbant.resonancelauncher.data.AppItem

@Composable
fun AppList(
    apps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(modifier = modifier) {
        if (title != null) {
            Text(
                modifier = Modifier.padding(
                    top = 27.dp,
                    start = 48.dp,
                    end = 48.dp,
                    bottom = 16.dp
                ),
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        LazyRow(
            modifier = Modifier,
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppCard(app = app, onClick = { onAppClick(app) })
            }
        }
    }
}