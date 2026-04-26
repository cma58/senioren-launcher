package com.seniorenlauncher.util

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.seniorenlauncher.BuildConfig
import com.seniorenlauncher.data.api.GitHubRelease
import com.seniorenlauncher.data.api.GitHubService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class UpdateManager(private val context: Context) {

    private val githubService: GitHubService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    suspend fun checkForUpdates(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val latestRelease = githubService.getLatestRelease()
            val currentVersion = BuildConfig.VERSION_NAME
            
            // Simpele vergelijking: als de tag op GitHub anders is dan onze versie
            // Voor productie zou je versie-parsing (semver) kunnen gebruiken
            if (latestRelease.tagName != currentVersion) {
                Log.d("UpdateManager", "Nieuwe versie beschikbaar: ${latestRelease.tagName}")
                return@withContext latestRelease
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Check for updates failed", e)
        }
        null
    }

    fun downloadAndInstall(release: GitHubRelease) {
        // EU Security: Controleer of we toestemming hebben om te installeren
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
            Log.w("UpdateManager", "Geen rechten om APK te installeren. Stuur gebruiker naar instellingen.")
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        val apkAsset = release.assets.find { it.name.endsWith(".apk") } ?: return
        
        // Forceer HTTPS voor maximale veiligheid (EU Privacy/Security eis)
        val downloadUri = if (apkAsset.downloadUrl.startsWith("http://")) {
            apkAsset.downloadUrl.replace("http://", "https://")
        } else {
            apkAsset.downloadUrl
        }

        val request = DownloadManager.Request(Uri.parse(downloadUri))
            .setTitle("Senioren Launcher Veiligheidsupdate")
            .setDescription("Nieuwe beveiligde versie ${release.tagName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "senioren-launcher-update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false) // Bespaar data voor de gebruiker (EU Duurzaamheid)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
