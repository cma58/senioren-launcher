package com.seniorenlauncher.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessagesViewModel : ViewModel() {
    private val _allConversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val conversations: StateFlow<List<Conversation>> = combine(_allConversations, _searchQuery) { list, query ->
        if (query.isBlank()) list
        else list.filter { 
            (it.contactName ?: it.address).contains(query, ignoreCase = true) || 
            it.snippet.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages

    private val _messageFontSizeMultiplier = MutableStateFlow(1.0f)
    val messageFontSizeMultiplier: StateFlow<Float> = _messageFontSizeMultiplier

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun adjustFontSize(delta: Float) {
        _messageFontSizeMultiplier.value = (_messageFontSizeMultiplier.value + delta).coerceIn(0.8f, 2.5f)
    }

    fun loadConversations(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val list = withContext(Dispatchers.IO) {
                    fetchConversationsInternal(context)
                }
                _allConversations.value = list
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
                markAsRead(context, address)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun markAsRead(context: Context, address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val values = android.content.ContentValues().apply {
                    put(Telephony.Sms.READ, 1)
                }
                context.contentResolver.update(
                    Telephony.Sms.CONTENT_URI,
                    values,
                    "${Telephony.Sms.ADDRESS} = ? AND ${Telephony.Sms.READ} = 0",
                    arrayOf(address)
                )
                // Refresh conversations list to update unread badges
                loadConversations(context)
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
                    
                    val values = android.content.ContentValues().apply {
                        put(Telephony.Sms.ADDRESS, address)
                        put(Telephony.Sms.BODY, body)
                        put(Telephony.Sms.DATE, System.currentTimeMillis())
                        put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                        put(Telephony.Sms.READ, 1)
                    }
                    context.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
                }
                _messages.value = listOf(SmsMessage(body, System.currentTimeMillis(), true)) + _messages.value
                loadConversations(context) // Update snippet in list
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
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms.THREAD_ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.READ),
                null, null, "${Telephony.Sms.DATE} DESC"
            )

            val seenThreads = mutableSetOf<Long>()

            cursor?.use {
                val threadIdIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                while (it.moveToNext()) {
                    val threadId = if (threadIdIdx != -1) it.getLong(threadIdIdx) else 0L
                    if (threadId == 0L || seenThreads.contains(threadId)) continue
                    seenThreads.add(threadId)

                    val address = if (addressIdx != -1) it.getString(addressIdx) else null
                    val snippet = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()
                    val isRead = if (readIdx != -1) it.getInt(readIdx) == 1 else true

                    if (address != null) {
                        val name = getContactName(context, address)
                        list.add(Conversation(address, snippet, date, name, threadId, isRead))
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
