package com.seniorenlauncher.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.widget.Toast

data class InstalledApp(val name: String, val packageName: String)

object AppLauncher {
    fun launchApp(context: Context, appId: String, customPackage: String? = null): Boolean {
        try {
            if (customPackage != null) {
                val intent = context.packageManager.getLaunchIntentForPackage(customPackage)
                if (intent != null) {
                    context.startActivity(intent)
                    return true
                }
            }

            val intent: Intent? = when (appId) {
                "phone" -> Intent(Intent.ACTION_DIAL)
                "sms" -> Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MESSAGING)
                }
                "whatsapp" -> context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                "video" -> context.packageManager.getLaunchIntentForPackage("com.google.android.apps.tachyon") // Meet
                    ?: context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                "camera" -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                "photos" -> Intent(Intent.ACTION_VIEW).apply {
                    type = "image/*"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                "alarm" -> Intent(AlarmClock.ACTION_SET_ALARM)
                "calendar" -> {
                    val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
                    Intent(Intent.ACTION_VIEW).setData(builder.build())
                }
                "weather" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buienradar.nl"))
                "notes" -> context.packageManager.getLaunchIntentForPackage("com.google.android.apps.notes") // Google Keep
                    ?: context.packageManager.getLaunchIntentForPackage("com.samsung.android.app.notes")
                "radio" -> context.packageManager.getLaunchIntentForPackage("com.google.android.apps.youtube.music")
                    ?: context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                else -> null
            }

            if (intent != null) {
                context.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Kon app niet openen", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    fun getInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 || pm.getLaunchIntentForPackage(it.packageName) != null }
            .map { InstalledApp(it.loadLabel(pm).toString(), it.packageName) }
            .sortedBy { it.name.lowercase() }
    }
}
