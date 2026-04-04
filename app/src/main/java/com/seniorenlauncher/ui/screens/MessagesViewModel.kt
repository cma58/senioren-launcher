package com.seniorenlauncher.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
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
                if (context.checkSelfPermission(android.Manifest.permission.READ_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val list = withContext(Dispatchers.IO) {
                        fetchConversationsInboxAndSent(context)
                    }
                    _allConversations.value = list
                    Log.d("SMS_FIX", "Gesprekken gevonden: ${list.size}")
                }
            } catch (e: Exception) {
                Log.e("SMS_FIX", "Laden mislukt", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchConversationsInboxAndSent(context: Context): List<Conversation> {
        val list = mutableListOf<Conversation>()
        val addressMap = mutableMapOf<String, Conversation>()
        val contentResolver = context.contentResolver
        
        try {
            // Op Android 16/15 scan we de gehele CONTENT_URI (bevat inbox + verzonden)
            // We gebruiken een brede query zonder groepering op database-niveau (dat blokkeert Android 16 soms)
            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.READ, Telephony.Sms.THREAD_ID),
                null, null, "date DESC"
            )

            cursor?.use {
                val addrIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                val readIdx = it.getColumnIndex(Telephony.Sms.READ)
                val threadIdx = it.getColumnIndex(Telephony.Sms.THREAD_ID)

                while (it.moveToNext() && addressMap.size < 100) {
                    val address = it.getString(addrIdx) ?: continue
                    
                    // We groeperen op het nummer. We kijken of we dit nummer al hebben gezien.
                    // We normaliseren het nummer naar de laatste 9 cijfers voor de match.
                    val normalizedKey = address.replace(Regex("[^0-9]"), "").takeLast(9).ifEmpty { address }
                    
                    if (!addressMap.containsKey(normalizedKey)) {
                        val snippet = it.getString(bodyIdx) ?: ""
                        val date = it.getLong(dateIdx)
                        val isRead = it.getInt(readIdx) == 1
                        val threadId = it.getLong(threadIdx)
                        val name = getContactName(context, address)
                        
                        val conv = Conversation(address, snippet, date, name, threadId, isRead)
                        addressMap[normalizedKey] = conv
                        list.add(conv)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SMS_FIX", "Rauwe scan gefaald", e)
        }
        return list.sortedByDescending { it.date }
    }

    fun loadMessages(context: Context, address: String) {
        viewModelScope.launch {
            try {
                val list = withContext(Dispatchers.IO) {
                    val messagesList = mutableListOf<SmsMessage>()
                    // We zoeken op de laatste 9 cijfers van het nummer. 
                    // Dit is de meest veilige match-methode na software updates.
                    val matchPart = address.replace(Regex("[^0-9]"), "").takeLast(9).ifEmpty { address }
                    val cursor = context.contentResolver.query(
                        Telephony.Sms.CONTENT_URI,
                        null,
                        "${Telephony.Sms.ADDRESS} LIKE ?",
                        arrayOf("%$matchPart"),
                        "date DESC"
                    )
                    cursor?.use {
                        val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                        val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
                        val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
                        while (it.moveToNext()) {
                            val body = it.getString(bodyIdx) ?: ""
                            val type = it.getInt(typeIdx)
                            val date = it.getLong(dateIdx)
                            messagesList.add(SmsMessage(body, date, type == Telephony.Sms.MESSAGE_TYPE_SENT))
                        }
                    }
                    messagesList
                }
                _messages.value = list
                markAsRead(context, address)
            } catch (e: Exception) {
                Log.e("SMS_FIX", "Berichten laden gefaald voor $address", e)
            }
        }
    }

    private fun markAsRead(context: Context, address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val matchPart = address.replace(Regex("[^0-9]"), "").takeLast(9).ifEmpty { address }
                val values = android.content.ContentValues().apply {
                    put(Telephony.Sms.READ, 1)
                }
                context.contentResolver.update(
                    Telephony.Sms.CONTENT_URI,
                    values,
                    "${Telephony.Sms.ADDRESS} LIKE ? AND ${Telephony.Sms.READ} = 0",
                    arrayOf("%$matchPart")
                )
                loadConversations(context)
            } catch (e: Exception) { }
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
                loadMessages(context, address)
                loadConversations(context)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Verzenden mislukt", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getContactName(context: Context, phoneNumber: String): String? {
        try {
            val uri = Uri.withAppendedPath(android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
            val cursor = context.contentResolver.query(uri, arrayOf(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(android.provider.ContactsContract.PhoneLookup.DISPLAY_NAME)
                    return it.getString(idx)
                }
            }
        } catch (e: Exception) { }
        return null
    }
}
