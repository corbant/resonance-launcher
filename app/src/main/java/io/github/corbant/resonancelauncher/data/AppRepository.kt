package io.github.corbant.resonancelauncher.data

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TvAppItem(
    val label: String,
    val packageName: String,
    val image: Drawable,
    val isBanner: Boolean,
    val launchIntent: Intent
)

class AppRepository(private val context: Context) {

    suspend fun getInstalledTvApps(): List<TvAppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager

        val tvIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(tvIntent, 0)

        resolveInfos.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName

            if (packageName == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(packageManager).toString()
            var banner = activityInfo.loadBanner(packageManager)
            if (banner == null) {
                banner = activityInfo.applicationInfo.loadBanner(packageManager)
            }
            val image = banner ?: resolveInfo.loadIcon(packageManager)
            val isBanner = banner != null

            val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(packageName)
                ?: packageManager.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                TvAppItem(
                    label = label,
                    packageName = packageName,
                    image = image,
                    isBanner = isBanner,
                    launchIntent = launchIntent
                )
            } else null
        }.sortedBy { it.label }
    }
}