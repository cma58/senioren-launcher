package com.seniorenlauncher.ui.screens

import android.app.role.RoleManager
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.Conversation
import com.seniorenlauncher.data.model.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {
    private val app = LauncherApp.instance

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages.asStateFlow()

    private val _isDefaultSmsApp = MutableStateFlow(false)
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentChatAddress = MutableStateFlow<String?>(null)
    
    private var smsObserver: android.database.ContentObserver? = null

    init {
        checkDefaultSmsApp()
        loadConversations()
        setupContentObserver()
    }

    fun checkDefaultSmsApp() {
        _isDefaultSmsApp.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = app.getSystemService(RoleManager::class.java)
            roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            @Suppress("DEPRECATION")
            Telephony.Sms.getDefaultSmsPackage(app) == app.packageName
        }
    }

    fun createDefaultSmsIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = app.getSystemService(RoleManager::class.java)
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            @Suppress("DEPRECATION")
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, app.packageName)
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val conversationsMap = mutableMapOf<Long, Conversation>()
                
                // We query all messages and group them by thread_id ourselves.
                // This is much more reliable on Android 14/15/16.
                val cursor = app.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    arrayOf(
                        Telephony.Sms.THREAD_ID,
                        Telephony.Sms.ADDRESS,
                        Telephony.Sms.BODY,
                        Telephony.Sms.DATE,
                        Telephony.Sms.READ,
                        Telephony.Sms.TYPE
                    ),
                    null, null, "date DESC"
                )

                cursor?.use {
                    val threadIdIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)
                    val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                    val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                    val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                    while (it.moveToNext()) {
                        val threadId = it.getLong(threadIdIdx)
                        if (!conversationsMap.containsKey(threadId)) {
                            val address = it.getString(addressIdx) ?: "Onbekend"
                            val snippet = it.getString(bodyIdx) ?: ""
                            val date = it.getLong(dateIdx)
                            val isRead = it.getInt(readIdx) == 1
                            val name = getContactName(address)
                            
                            conversationsMap[threadId] = Conversation(
                                address = address,
                                snippet = snippet,
                                date = date,
                                contactName = name,
                                threadId = threadId,
                                isRead = isRead
                            )
                        }
                        // Stop after 50 conversations to keep it fast
                        if (conversationsMap.size >= 50) break
                    }
                }
                _conversations.value = conversationsMap.values.sortedByDescending { it.date }
            } catch (e: Exception) {
                Log.e("SMS_VM", "Fout bij laden gesprekken", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadChat(address: String) {
        _currentChatAddress.value = address
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = mutableListOf<SmsMessage>()
                // We use a broader match for the address to handle different formatting
                val matchPart = address.replace(Regex("[^0-9]"), "").takeLast(9).ifEmpty { address }
                val selection = "${Telephony.Sms.ADDRESS} LIKE ?"
                val selectionArgs = arrayOf("%$matchPart")
                
                val cursor = app.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    null, selection, selectionArgs, "date DESC"
                )

                cursor?.use {
                    val idIdx = it.getColumnIndex(Telephony.Sms._ID)
                    val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                    val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
                    val readIdx = it.getColumnIndex(Telephony.Sms.READ)

                    while (it.moveToNext()) {
                        val type = it.getInt(typeIdx)
                        list.add(SmsMessage(
                            id = it.getLong(idIdx),
                            address = address,
                            body = it.getString(bodyIdx) ?: "",
                            timestamp = it.getLong(dateIdx),
                            isSent = type == Telephony.Sms.MESSAGE_TYPE_SENT,
                            isRead = it.getInt(readIdx) == 1
                        ))
                    }
                }
                _messages.value = list
                markAsRead(address)
            } catch (e: Exception) {
                Log.e("SMS_VM", "Fout bij laden chat", e)
            }
        }
    }

    private fun markAsRead(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val matchPart = address.replace(Regex("[^0-9]"), "").takeLast(9).ifEmpty { address }
                val values = ContentValues().apply { put(Telephony.Sms.READ, 1) }
                app.contentResolver.update(
                    Telephony.Sms.CONTENT_URI,
                    values,
                    "${Telephony.Sms.ADDRESS} LIKE ? AND ${Telephony.Sms.READ} = 0",
                    arrayOf("%$matchPart")
                )
            } catch (e: Exception) {
                Log.e("SMS_VM", "Markeren als gelezen mislukt", e)
            }
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                app.contentResolver.delete(
                    Telephony.Sms.CONTENT_URI,
                    "${Telephony.Sms._ID} = ?",
                    arrayOf(id.toString())
                )
                _currentChatAddress.value?.let { loadChat(it) } ?: loadConversations()
            } catch (e: Exception) {
                Log.e("SMS_VM", "Bericht wissen mislukt", e)
            }
        }
    }

    fun sendMessage(address: String, body: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val smsManager = app.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(address, null, body, null, null)

                val values = ContentValues().apply {
                    put(Telephony.Sms.ADDRESS, address)
                    put(Telephony.Sms.BODY, body)
                    put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
                    put(Telephony.Sms.DATE, System.currentTimeMillis())
                    put(Telephony.Sms.READ, 1)
                }
                app.contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
                
                _currentChatAddress.value?.let { loadChat(it) }
                loadConversations()
            } catch (e: Exception) {
                Log.e("SMS_VM", "Verzenden mislukt", e)
            }
        }
    }

    private fun setupContentObserver() {
        smsObserver = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                viewModelScope.launch(Dispatchers.IO) {
                    loadConversations()
                    _currentChatAddress.value?.let { loadChat(it) }
                }
            }
        }
        app.contentResolver.registerContentObserver(Telephony.Sms.CONTENT_URI, true, smsObserver!!)
    }

    override fun onCleared() {
        super.onCleared()
        smsObserver?.let { app.contentResolver.unregisterContentObserver(it) }
    }

    private fun getContactName(phoneNumber: String): String? {
        try {
            val uri = Uri.withAppendedPath(android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val cursor = app.contentResolver.query(uri, arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (idx >= 0) return it.getString(idx)
                }
            }
        } catch (e: Exception) {
            Log.e("SMS_VM", "Contactnaam ophalen mislukt", e)
        }
        return null
    }

    fun clearActiveChat() {
        _currentChatAddress.value = null
    }
}
