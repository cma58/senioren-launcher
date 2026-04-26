package com.seniorenlauncher.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.seniorenlauncher.util.UpdateManager
import java.io.File

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            Log.d("UpdateReceiver", "Download complete: $downloadId")
            
            val updateFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "senioren-launcher-update.apk"
            )
            
            if (updateFile.exists()) {
                Log.d("UpdateReceiver", "Update file found, starting installation")
                UpdateManager(context).installApk(updateFile)
            } else {
                // Probeer ook de publieke download map als fallback
                val publicFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "senioren-launcher-update.apk"
                )
                if (publicFile.exists()) {
                    UpdateManager(context).installApk(publicFile)
                }
            }
        }
    }
}
