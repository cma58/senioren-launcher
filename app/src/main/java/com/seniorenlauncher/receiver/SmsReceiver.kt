package com.seniorenlauncher.receiver

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.MainActivity

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Telephony.Sms.Intents.SMS_DELIVER_ACTION || action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val address = sms.displayOriginatingAddress ?: "Onbekend"
                val body = sms.displayMessageBody
                val timestamp = sms.timestampMillis

                Log.d("SmsReceiver", "Bericht ontvangen van $address: $body")

                // Alleen opslaan in Inbox als we de Default SMS app zijn
                val isDefault = Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
                if (isDefault || action == Telephony.Sms.Intents.SMS_DELIVER_ACTION) {
                    val values = ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, address)
                        put(Telephony.Sms.BODY, body)
                        put(Telephony.Sms.DATE, timestamp)
                        put(Telephony.Sms.READ, 0)
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
                    }
                    try {
                        context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
                        showNotification(context, address, body)
                    } catch (e: Exception) {
                        Log.e("SmsReceiver", "Fout bij opslaan in database", e)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, address: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("NAVIGATE_TO", "sms")
            putExtra("SMS_ADDRESS", address)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            System.currentTimeMillis().toInt(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, LauncherApp.CH_SMS)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(address)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Belangrijk voor Android 15/16 voor direct zichtbaarheid
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Voor Android 15+ dwingen we geluid via de builder als kanaal-instellingen niet worden opgepakt
        if (Build.VERSION.SDK_INT >= 35) {
             builder.setSound(defaultSoundUri)
        }

        notificationManager.notify(address.hashCode(), builder.build())
    }
}
