package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.unit.dp
import io.github.corbant.resonancelauncher.model.AppItem

@Composable
fun AppGrid(
    apps: List<AppItem>,
    onLaunchApp: (packageName: String) -> Unit,
    modifier: Modifier = Modifier,
    onAppLongClick: ((AppItem) -> Unit)? = null
) {
    val firstItemRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        firstItemRequester.requestFocus()
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        contentPadding = PaddingValues(32.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.focusRestorer(
            fallback = firstItemRequester
        )
    ) {
        itemsIndexed(apps, key = { _, it -> it.packageName }) { index, app ->
            CircularAppIcon(
                app = app,
                onClick = { onLaunchApp(app.packageName) },
                onLongClick = if (onAppLongClick != null) { { onAppLongClick(app) } } else null,
                modifier = Modifier
                    .animateItem(
                        fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .then(
                        if (index == 0) {
                            Modifier.focusRequester(firstItemRequester)
                        } else Modifier
                    )
            )
        }
    }
}