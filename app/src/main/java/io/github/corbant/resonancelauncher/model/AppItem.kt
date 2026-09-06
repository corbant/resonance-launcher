package io.github.corbant.resonancelauncher.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable

@Immutable
data class AppItem(
    val label: String,
    val packageName: String,
    val iconBitmap: Bitmap,
    val dominantColor: Int
)