package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.data.model.Conversation
import com.seniorenlauncher.data.model.SmsMessage
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    messagesVm: MessagesViewModel = viewModel(),
    initialAddress: String? = null
) {
    val isDefaultSms by messagesVm.isDefaultSmsApp.collectAsState()
    val context = LocalContext.current
    
    // Permission state
    var hasSmsPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        hasSmsPermissions = results.values.all { it }
        if (hasSmsPermissions) {
            messagesVm.loadConversations()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasSmsPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS
                )
            )
        }
    }

    // Role Manager Launcher
    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        messagesVm.checkDefaultSmsApp()
    }

    var activeView by remember { mutableStateOf(MessageView.Overview) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }

    // Handle deep link
    LaunchedEffect(initialAddress) {
        if (initialAddress != null) {
            selectedAddress = initialAddress
            selectedName = null // We'll look this up via the conversation list if needed, or just show number
            activeView = MessageView.Chat
        }
    }

    val backHandler = {
        when (activeView) {
            MessageView.Overview -> onBack()
            MessageView.Chat -> {
                activeView = MessageView.Overview
                selectedAddress = null
                messagesVm.clearActiveChat()
            }
            MessageView.NewMessage -> activeView = MessageView.Overview
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(
            title = when (activeView) {
                MessageView.Overview -> "Berichten"
                MessageView.Chat -> selectedName ?: selectedAddress ?: "Chat"
                MessageView.NewMessage -> "Nieuw Bericht"
            },
            onBack = backHandler
        )

        if (!hasSmsPermissions) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("De app heeft toestemming nodig om uw berichten te tonen.", textAlign = TextAlign.Center, fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { 
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)) 
                    }) {
                        Text("GEEF TOESTEMMING", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            if (!isDefaultSms) {
                DefaultSmsBanner {
                    val intent = messagesVm.createDefaultSmsIntent()
                    if (intent != null) roleLauncher.launch(intent)
                }
            }

            Box(Modifier.weight(1f)) {
                when (activeView) {
                    MessageView.Overview -> {
                        ConversationOverview(
                            messagesVm = messagesVm,
                            onConversationClick = { conv ->
                                selectedAddress = conv.address
                                selectedName = conv.contactName
                                activeView = MessageView.Chat
                            },
                            onNewMessageClick = { activeView = MessageView.NewMessage }
                        )
                    }
                    MessageView.Chat -> {
                        ChatScreen(
                            address = selectedAddress!!,
                            messagesVm = messagesVm
                        )
                    }
                    MessageView.NewMessage -> {
                        NewMessageScreen(
                            messagesVm = messagesVm,
                            onContactSelected = { name, phone ->
                                selectedAddress = phone
                                selectedName = name
                                activeView = MessageView.Chat
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class MessageView { Overview, Chat, NewMessage }

@Composable
fun DefaultSmsBanner(onSetDefault: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "De app is niet de standaard SMS-app. U kunt geen berichten ontvangen of wissen.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSetDefault,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("MAAK STANDAARD", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun ConversationOverview(
    messagesVm: MessagesViewModel,
    onConversationClick: (Conversation) -> Unit,
    onNewMessageClick: () -> Unit
) {
    val conversations by messagesVm.conversations.collectAsState()
    val isLoading by messagesVm.isLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        messagesVm.loadConversations()
    }

    Column(Modifier.fillMaxSize()) {
        Button(
            onClick = onNewMessageClick,
            modifier = Modifier.fillMaxWidth().height(70.dp).padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text("NIEUW BERICHT MAKEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
        }

        if (isLoading && conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Geen berichten gevonden.", fontSize = 20.sp, color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
                items(conversations) { conv ->
                    ConversationItem(conv, onConversationClick)
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: (Conversation) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(conv) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (conv.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (conv.contactName ?: conv.address).take(1).uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conv.contactName ?: conv.address,
                        fontSize = 22.sp,
                        fontWeight = if (conv.isRead) FontWeight.Bold else FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!conv.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color.Red))
                    }
                }
                Text(
                    conv.snippet,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatTime(conv.date),
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ChatScreen(address: String, messagesVm: MessagesViewModel) {
    val messages by messagesVm.messages.collectAsState()
    var newMessageBody by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()
    var messageToDelete by remember { mutableStateOf<SmsMessage?>(null) }

    LaunchedEffect(address) {
        messagesVm.loadChat(address)
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(
                    message = msg,
                    onDelete = { messageToDelete = msg }
                )
            }
        }

        Row(
            Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = newMessageBody,
                onValueChange = { newMessageBody = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Schrijf bericht...", fontSize = 20.sp) },
                shape = RoundedCornerShape(24.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
            )
            FloatingActionButton(
                onClick = {
                    if (newMessageBody.isNotBlank()) {
                        messagesVm.sendMessage(address, newMessageBody)
                        newMessageBody = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(60.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Send, null, modifier = Modifier.size(32.dp))
            }
        }
    }

    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Bericht wissen?") },
            text = { Text("Weet u zeker dat u dit bericht wilt verwijderen?") },
            confirmButton = {
                Button(
                    onClick = {
                        messagesVm.deleteMessage(messageToDelete!!.id)
                        messageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("WISSEN")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("ANNULEREN")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(message: SmsMessage, onDelete: () -> Unit) {
    val alignment = if (message.isSent) Alignment.End else Alignment.Start
    val color = if (message.isSent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isSent) 20.dp else 0.dp,
                bottomEnd = if (message.isSent) 0.dp else 20.dp
            ),
            modifier = Modifier.padding(horizontal = 8.dp).widthIn(max = 320.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.body,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 28.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, null, tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatDateTime(message.timestamp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = if (message.isSent) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun NewMessageScreen(messagesVm: MessagesViewModel, onContactSelected: (String, String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            withContext(Dispatchers.IO) {
                val list = mutableListOf<Pair<String, String>>()
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val pIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        if (nIdx >= 0 && pIdx >= 0) {
                            list.add(it.getString(nIdx) to it.getString(pIdx))
                        }
                    }
                }
                contacts = list.distinctBy { it.second.replace(" ", "") }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Typ naam of nummer...", fontSize = 20.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (searchQuery.length >= 3 && searchQuery.all { it.isDigit() || it == '+' }) {
            Button(
                onClick = { onContactSelected(searchQuery, searchQuery) },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("STUUR NAAR $searchQuery", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(contacts.filter { it.first.contains(searchQuery, true) || it.second.contains(searchQuery) }) { contact ->
                Card(
                    Modifier.fillMaxWidth().clickable { onContactSelected(contact.first, contact.second) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            Text(contact.first.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(contact.first, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(contact.second, fontSize = 16.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

fun formatDateTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }
    return if (now.get(Calendar.DATE) == msgTime.get(Calendar.DATE)) {
        "Vandaag " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    } else {
        SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
