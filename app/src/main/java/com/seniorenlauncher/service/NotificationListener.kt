package com.seniorenlauncher.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
        private val _activeNotifications = MutableStateFlow<List<StatusBarNotification>>(emptyList())
        val activeNotificationsFlow: StateFlow<List<StatusBarNotification>> = _activeNotifications

        var instance: NotificationListener? = null
            private set

        fun getBadgeCountByAppId(appId: String, appMappings: Map<String, String>, counts: Map<String, Int>): Int {
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
                    (counts["com.samsung.android.messaging"] ?: 0) +
                    (counts["com.seniorenlauncher"] ?: 0)
                }
                "whatsapp" -> counts["com.whatsapp"] ?: 0
                else -> {
                    val pkg = appMappings[appId]
                    if (pkg != null) counts[pkg] ?: 0 else 0
                }
            }
        }
        
        // Legacy support for badge counts
        private val _notifications = MutableStateFlow<Map<String, Int>>(emptyMap())
        val notifications: StateFlow<Map<String, Int>> = _notifications

        fun dismissAll() {
            instance?.cancelAllNotifications()
        }

        fun dismiss(key: String) {
            try {
                instance?.cancelNotification(key)
            } catch (e: Exception) {
                Log.e(TAG, "Error dismissing notification: $key", e)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "Notification posted from: ${sbn?.packageName}")
        updateState()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d(TAG, "Notification removed from: ${sbn?.packageName}")
        updateState()
    }

    override fun onListenerConnected() {
        Log.d(TAG, "Notification listener connected")
        instance = this
        updateState()
    }

    override fun onListenerDisconnected() {
        Log.d(TAG, "Notification listener disconnected")
        instance = null
    }

    private fun updateState() {
        try {
            val active = try { activeNotifications } catch (e: Exception) { null } ?: emptyArray()
            
            // Filter logic: Only communication, important alerts, and non-ongoing
            val filtered = active.filter { sbn ->
                val isOngoing = sbn.isOngoing
                val isSystem = sbn.packageName == "android" || sbn.packageName == "com.android.systemui"
                val category = sbn.notification.category
                
                val isCommunication = category == Notification.CATEGORY_MESSAGE || 
                                     category == Notification.CATEGORY_CALL ||
                                     category == Notification.CATEGORY_EMAIL ||
                                     category == Notification.CATEGORY_EVENT
                
                val importantPackages = listOf("com.whatsapp", "com.google.android.apps.messaging", "com.android.mms", "com.seniorenlauncher")
                val isImportantApp = sbn.packageName in importantPackages
                
                !isOngoing && (!isSystem || isCommunication) && (isCommunication || isImportantApp || category == Notification.CATEGORY_REMINDER)
            }
            
            _activeNotifications.value = filtered
            
            // Update legacy badge counts
            val counts = mutableMapOf<String, Int>()
            filtered.forEach { sbn ->
                counts[sbn.packageName] = (counts[sbn.packageName] ?: 0) + 1
            }
            _notifications.value = counts
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification state", e)
        }
    }
}
