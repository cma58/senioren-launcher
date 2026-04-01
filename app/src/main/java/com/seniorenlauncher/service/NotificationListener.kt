package com.seniorenlauncher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        private val _notifications = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notifications: StateFlow<Map<String, Int>> = _notifications

        fun getBadgeCountByAppId(appId: String, appMappings: Map<String, String>, counts: Map<String, Int>): Int {
            // We check for multiple possible package names for system apps like Phone and SMS
            return when (appId) {
                "phone" -> {
                    (counts["com.android.server.telecom"] ?: 0) +
                    (counts["com.google.android.dialer"] ?: 0) +
                    (counts["com.android.dialer"] ?: 0) +
                    (counts["com.samsung.android.dialer"] ?: 0)
                }
                "sms" -> {
                    (counts["com.google.android.apps.messaging"] ?: 0) +
                    (counts["com.android.mms"] ?: 0) +
                    (counts["com.samsung.android.messaging"] ?: 0)
                }
                "whatsapp" -> counts["com.whatsapp"] ?: 0
                else -> {
                    val pkg = appMappings[appId]
                    if (pkg != null) counts[pkg] ?: 0 else 0
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "Notification posted from: ${sbn?.packageName}")
        updateBadgeCounts()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d(TAG, "Notification removed from: ${sbn?.packageName}")
        updateBadgeCounts()
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Notification listener connected")
        updateBadgeCounts()
    }

    private fun updateBadgeCounts() {
        try {
            val activeNotifications = try { activeNotifications } catch (e: Exception) { null }
            val counts = mutableMapOf<String, Int>()
            
            activeNotifications?.forEach { sbn ->
                val pkg = sbn.packageName
                // Important: many apps have multiple notifications (e.g. one per chat)
                // We count only non-ongoing (clearable) notifications that are not system noise
                if (!sbn.isOngoing && sbn.id != 0) {
                    counts[pkg] = (counts[pkg] ?: 0) + 1
                }
            }
            _notifications.value = counts
            Log.d(TAG, "Updated counts: $counts")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating counts", e)
        }
    }
}
