package io.github.corbant.resonancelauncher.ui.features.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun HomeRow(
    modifier: Modifier = Modifier,
    title: String,
    content: LazyListScope.(fallbackFocusRequestor: FocusRequester) -> Unit
) {
    val fallbackFocusRequester = remember { FocusRequester() }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            modifier = Modifier.padding(start = 48.dp),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.focusRestorer(
                fallback = fallbackFocusRequester
            )
        ) {
            content(fallbackFocusRequester)
        }
    }
}