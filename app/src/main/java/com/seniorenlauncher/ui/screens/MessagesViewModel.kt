package com.seniorenlauncher.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesViewModel : ViewModel() {
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages

    fun loadConversations(context: Context) {
        if (_isLoading.value) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val list = withContext(Dispatchers.IO) {
                    fetchConversationsInternal(context)
                }
                _conversations.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMessages(context: Context, address: String) {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    fetchMessagesForAddressInternal(context, address)
                }
                _messages.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendMessage(context: Context, address: String, body: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(address, null, body, null, null)
                }
                _messages.value = listOf(SmsMessage(body, System.currentTimeMillis(), true)) + _messages.value
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Verzenden mislukt: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchConversationsInternal(context: Context): List<Conversation> {
        val list = mutableListOf<Conversation>()
        try {
            // Use Telephony.Sms.CONTENT_URI for better compatibility
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE),
                null, null, "${Telephony.Sms.DATE} DESC"
            )

            val seenThreads = mutableSetOf<Long>()

            cursor?.use {
                val threadIdIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val threadId = if (threadIdIdx != -1) it.getLong(threadIdIdx) else 0L
                    if (threadId == 0L || seenThreads.contains(threadId)) continue
                    seenThreads.add(threadId)

                    val address = if (addressIdx != -1) it.getString(addressIdx) else null
                    val snippet = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()

                    if (address != null) {
                        val name = getContactName(context, address)
                        list.add(Conversation(address, snippet, date, name, threadId))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun fetchMessagesForAddressInternal(context: Context, address: String): List<SmsMessage> {
        val list = mutableListOf<SmsMessage>()
        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null,
                "${Telephony.Sms.ADDRESS} = ?",
                arrayOf(address),
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext()) {
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val type = if (typeIdx != -1) it.getInt(typeIdx) else Telephony.Sms.MESSAGE_TYPE_INBOX
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()
                    list.add(SmsMessage(body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        try {
            val uri = Uri.withAppendedPath(android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val cursor = context.contentResolver.query(uri, arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx != -1) return it.getString(idx)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
