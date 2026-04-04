package com.seniorenlauncher.data.model

data class SmsMessage(
    val id: Long,
    val address: String,
    val body: String,
    val timestamp: Long,
    val isSent: Boolean,
    val isRead: Boolean
)

data class Conversation(
    val address: String,
    val snippet: String,
    val date: Long,
    val contactName: String? = null,
    val threadId: Long = 0,
    val isRead: Boolean = true,
    val unreadCount: Int = 0
)
