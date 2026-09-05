package io.github.corbant.resonancelauncher.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.runtime.Immutable
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
data class AppItem(
    val label: String,
    val packageName: String,
    val iconBitmap: Bitmap,
    val launchIntent: Intent,
    val dominantColor: Int
)

class AppRepository(private val context: Context) {

    private val excludedPackages = setOf(
        "com.android.vending", // Google Play Store
        "com.android.tv.settings" // Android TV Settings
    )

    suspend fun getInstalledTvApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager

        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(tvIntent, 0)

        resolveInfos.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName

            if (packageName == context.packageName || packageName in excludedPackages) return@mapNotNull null

            val label = resolveInfo.loadLabel(packageManager).toString()
            val icon = activityInfo.loadIcon(packageManager)
            val bitmap = icon.toBitmap()

            val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
                ?: packageManager.getLaunchIntentForPackage(packageName)

            val dominantColor = try {
                val palette = Palette.from(bitmap).generate()
                palette.getDominantColor(Color.BLUE)
            } catch (e: Exception) {
                Color.BLUE
            }

            if (launchIntent != null) {
                AppItem(
                    label = label,
                    packageName = packageName,
                    iconBitmap = bitmap,
                    launchIntent = launchIntent,
                    dominantColor = dominantColor
                )
            } else null
        }.sortedBy { it.label }
    }
}