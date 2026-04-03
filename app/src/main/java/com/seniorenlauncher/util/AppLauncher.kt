package com.seniorenlauncher.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast

data class InstalledApp(val name: String, val packageName: String, val icon: Drawable? = null)

object AppLauncher {
    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error launching app: $packageName", e)
            false
        }
    }

    fun openSystemCamera(context: Context) {
        try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback for some devices
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(cameraIntent)
            }
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error opening camera", e)
            Toast.makeText(context, "Kan camera niet openen", Toast.LENGTH_SHORT).show()
        }
    }

    fun getInstalledApps(context: Context, includeIcons: Boolean = false): List<InstalledApp> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter { 
            (pm.getLaunchIntentForPackage(it.packageName) != null)
        }
        .map { 
            InstalledApp(
                it.loadLabel(pm).toString(), 
                it.packageName,
                if (includeIcons) it.loadIcon(pm) else null
            ) 
        }
        .sortedBy { it.name.lowercase() }
    }

    fun getAppInfo(context: Context, packageName: String): InstalledApp? {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            InstalledApp(
                appInfo.loadLabel(pm).toString(),
                packageName,
                appInfo.loadIcon(pm)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    fun startRemoteSupport(context: Context) {
        val packageName = "com.carriez.flutter_hbb"
        try {
            val pm = context.packageManager
            pm.getPackageInfo(packageName, 0)
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                openGitHubReleases(context)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            openGitHubReleases(context)
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error starting remote support", e)
            Toast.makeText(context, "Fout bij starten hulp op afstand", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGitHubReleases(context: Context) {
        try {
            val githubIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rustdesk/rustdesk/releases/latest"))
            githubIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(githubIntent)
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error opening GitHub releases", e)
            Toast.makeText(context, "Kan GitHub niet openen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlayStore(context: Context, packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
