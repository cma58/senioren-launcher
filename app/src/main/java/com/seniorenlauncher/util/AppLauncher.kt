package com.seniorenlauncher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error opening camera", e)
            Toast.makeText(context, "Kan camera niet openen", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun getInstalledApps(context: Context, includeIcons: Boolean = false): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        apps.filter { 
            (pm.getLaunchIntentForPackage(it.packageName) != null) && it.packageName != context.packageName
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
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "RustDesk niet gevonden", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error starting remote support", e)
        }
    }
}
