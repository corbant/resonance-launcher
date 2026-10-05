package io.github.corbant.resonancelauncher.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

fun Context.isNotificationListenerEnabled(): Boolean {
    val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return flat?.contains(packageName) == true
}

fun Context.launchNotificationListenerSettings() {
    val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        startActivity(intent)
    } catch (_: Exception) {
        val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(fallbackIntent)
        } catch (_: Exception) {
            Toast.makeText(this, "Could not open settings", Toast.LENGTH_SHORT).show()
        }
    }
}
