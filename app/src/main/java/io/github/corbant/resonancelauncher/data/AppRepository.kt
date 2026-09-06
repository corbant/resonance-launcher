package io.github.corbant.resonancelauncher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import io.github.corbant.resonancelauncher.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    companion object {
        const val PACKAGE_PLAY_STORE = "com.android.vending"
        const val PACKAGE_TV_SETTINGS = "com.android.tv.settings"
    }

    private val excludedPackages = setOf(
        PACKAGE_PLAY_STORE,
        PACKAGE_TV_SETTINGS
    )

    suspend fun getInstalledApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager

        val leanbackIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val leanbackApps =
            packageManager.queryIntentActivities(leanbackIntent, PackageManager.MATCH_ALL)

        val standardIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val standardApps =
            packageManager.queryIntentActivities(standardIntent, PackageManager.MATCH_ALL)
                .filter { resolveInfo ->
                    // exclude pre-installed system apps that do not have a leanback intent
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystemApp =
                        (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                    !isSystemApp && !isUpdatedSystemApp
                }

        val installedApps = (leanbackApps + standardApps).distinctBy {
            it.activityInfo.packageName
        }

        installedApps.mapNotNull { resolveInfo ->
            val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
            val packageName = activityInfo.packageName

            if (packageName == context.packageName || packageName in excludedPackages) return@mapNotNull null

            val canLaunch =
                packageManager.getLeanbackLaunchIntentForPackage(packageName) != null ||
                        packageManager.getLaunchIntentForPackage(packageName) != null

            if (!canLaunch) return@mapNotNull null

            val label = resolveInfo.loadLabel(packageManager).toString()
            val icon = activityInfo.loadIcon(packageManager)
            val bitmap = icon.toBitmap()

            val dominantColor = try {
                val palette = Palette.from(bitmap).generate()
                palette.getDominantColor(Color.BLUE)
            } catch (e: Exception) {
                Color.BLUE
            }

            AppItem(
                label = label,
                packageName = packageName,
                iconBitmap = bitmap,
                dominantColor = dominantColor
            )
        }.sortedBy { it.label }
    }

    fun observeInstalledApps(): Flow<List<AppItem>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                launch {
                    trySend(getInstalledApps())
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        context.registerReceiver(receiver, filter)
        
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}