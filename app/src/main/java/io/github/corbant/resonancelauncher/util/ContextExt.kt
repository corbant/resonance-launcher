package io.github.corbant.resonancelauncher.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri

fun Context.launchAppByPackage(packageName: String) {
    val pm = packageManager
    val launchIntent =
        pm.getLeanbackLaunchIntentForPackage(packageName)
            ?: pm.getLaunchIntentForPackage(packageName)

    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(launchIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open app", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(this, "App cannot be launched", Toast.LENGTH_SHORT).show()
    }
}

fun Context.launchSystemSettings() {
    val intent = Intent(Settings.ACTION_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "Could not open system settings", Toast.LENGTH_SHORT).show()
    }
}

fun Context.launchAppStore() {
    val pm = packageManager

    val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "https://play.google.com/store/apps".toUri()
        setPackage("com.android.vending")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (playStoreIntent.resolveActivity(pm) != null) {
        startActivity(playStoreIntent)
        return
    }

    val amazonStoreIntent = Intent(Intent.ACTION_VIEW).apply {
        data = "amzn://apps/android?s=".toUri()
        setPackage("com.amazon.venezia")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (amazonStoreIntent.resolveActivity(pm) != null) {
        startActivity(amazonStoreIntent)
        return
    }

    val genericMarketIntent = Intent(Intent.ACTION_VIEW, "market://search?q=".toUri()).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (genericMarketIntent.resolveActivity(pm) != null) {
        startActivity(genericMarketIntent)
        return
    }

    Toast.makeText(this, "No supported app store found", Toast.LENGTH_SHORT).show()
}

fun Context.uninstallAppByPackage(packageName: String) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:$packageName".toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(this, "Could not launch uninstaller", Toast.LENGTH_SHORT).show()
    }
}