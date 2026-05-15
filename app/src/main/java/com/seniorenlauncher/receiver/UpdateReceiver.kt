package com.seniorenlauncher.receiver

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.seniorenlauncher.util.UpdateManager
import java.io.File

class UpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId == -1L) return

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIdx)
                
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val uriIdx = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                    val uriString = cursor.getString(uriIdx)
                    
                    if (uriString != null) {
                        val file = File(Uri.parse(uriString).path ?: "")
                        if (file.exists()) {
                            Log.d("UpdateReceiver", "Update file found at ${file.absolutePath}, starting installation")
                            UpdateManager(context).installApk(file)
                        } else {
                            // Fallback for Scoped Storage / Android 11+
                            try {
                                val pfd = downloadManager.openDownloadedFile(downloadId)
                                if (pfd != null) {
                                    // Als we hier zijn, is het bestand gedownload. 
                                    // De installApk methode in UpdateManager gebruikt FileProvider.
                                    // We moeten zorgen dat het bestand in de app-specifieke map staat (wat nu geregeld is in UpdateManager).
                                    val updateFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "senioren-launcher-update.apk")
                                    if (updateFile.exists()) {
                                        UpdateManager(context).installApk(updateFile)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("UpdateReceiver", "Error opening downloaded file", e)
                            }
                        }
                    }
                }
            }
            cursor.close()
        }
    }
}
