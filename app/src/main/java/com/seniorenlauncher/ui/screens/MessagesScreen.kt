package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
    var textZoom by remember { mutableFloatStateOf(1f) }

    var hasSmsPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        hasSmsPermissions = results.values.all { it }
        if (hasSmsPermissions) messagesVm.loadConversations()
    }

    LaunchedEffect(Unit) {
        if (!hasSmsPermissions) {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS))
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        messagesVm.checkDefaultSmsApp()
    }

    var activeView by remember { mutableStateOf(MessageView.Overview) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialAddress) {
        if (!initialAddress.isNullOrBlank()) {
            selectedAddress = initialAddress
            activeView = MessageView.Chat
        }
    }

    val backHandler = {
        when (activeView) {
            MessageView.Overview -> onBack()
            MessageView.Chat -> { activeView = MessageView.Overview; selectedAddress = null; messagesVm.clearActiveChat() }
            MessageView.NewMessage -> activeView = MessageView.Overview
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.weight(1f)) {
                    ScreenHeader(
                        title = when (activeView) {
                            MessageView.Overview -> "Berichten"
                            MessageView.Chat -> selectedName ?: selectedAddress ?: "Chat"
                            MessageView.NewMessage -> "Nieuw"
                        },
                        onBack = backHandler
                    )
                }
                
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(40.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                        IconButton(onClick = { if (textZoom > 0.8f) textZoom -= 0.1f }) {
                            Icon(Icons.Default.Remove, "Kleiner", tint = Color.White)
                        }
                        Text("A", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        IconButton(onClick = { if (textZoom < 1.8f) textZoom += 0.1f }) {
                            Icon(Icons.Default.Add, "Groter", tint = Color.White)
                        }
                    }
                }
            }

            if (!hasSmsPermissions) {
                PermissionRequestView { permissionLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS)) }
            } else {
                if (!isDefaultSms) {
                    DefaultSmsBanner { messagesVm.createDefaultSmsIntent()?.let { roleLauncher.launch(it) } }
                }

                Box(Modifier.weight(1f)) {
                    AnimatedContent(targetState = activeView, label = "ViewTransition") { view ->
                        when (view) {
                            MessageView.Overview -> ConversationOverview(
                                messagesVm = messagesVm,
                                onConversationClick = { conv ->
                                    selectedAddress = conv.address
                                    selectedName = conv.contactName
                                    activeView = MessageView.Chat
                                },
                                onNewMessageClick = { activeView = MessageView.NewMessage },
                                zoom = textZoom
                            )
                            MessageView.Chat -> ChatScreen(address = selectedAddress ?: "", messagesVm = messagesVm, zoom = textZoom)
                            MessageView.NewMessage -> NewMessageScreen(
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
}

enum class MessageView { Overview, Chat, NewMessage }

@Composable
fun ConversationOverview(messagesVm: MessagesViewModel, onConversationClick: (Conversation) -> Unit, onNewMessageClick: () -> Unit, zoom: Float) {
    val conversations by messagesVm.conversations.collectAsState()
    val isLoading by messagesVm.isLoading.collectAsState()

    LaunchedEffect(Unit) { messagesVm.loadConversations() }

    Column(Modifier.fillMaxSize()) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.94f else 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
            label = "scale"
        )
        Surface(
            onClick = onNewMessageClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(80.dp)
                .scale(scale),
            color = Color(0xFF6366F1),
            shape = RoundedCornerShape(40.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            shadowElevation = 8.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("NIEUW BERICHT SCHRIJVEN", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }

        if (isLoading && conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(conversations) { conv ->
                    ConversationItem(conv, onConversationClick, zoom)
                }
            }
        }
    }
}

@Composable
fun ConversationItem(conv: Conversation, onClick: (Conversation) -> Unit, zoom: Float) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "scale"
    )

    Surface(
        onClick = { onClick(conv) },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(40.dp),
        color = if (conv.isRead) Color(0xFF1E293B) else Color(0xFF334155),
        border = BorderStroke(1.dp, if (conv.isRead) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().scale(scale)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size((64 * zoom).dp)
                    .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text((conv.contactName ?: conv.address).take(1).uppercase(), fontSize = (28 * zoom).sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conv.contactName ?: conv.address,
                        fontSize = (22 * zoom).sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!conv.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(12.dp).background(Color(0xFF10B981), CircleShape))
                    }
                }
                Text(conv.snippet, fontSize = (18 * zoom).sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White.copy(alpha = 0.6f))
            }
            Text(formatTime(conv.date), fontSize = (14 * zoom).sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun ChatScreen(address: String, messagesVm: MessagesViewModel, zoom: Float) {
    val messages by messagesVm.messages.collectAsState()
    var newMessageBody by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    LaunchedEffect(address) { messagesVm.loadChat(address) }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(message = msg, zoom = zoom)
            }
        }

        Surface(
            modifier = Modifier.padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            shape = RoundedCornerShape(40.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = newMessageBody,
                    onValueChange = { newMessageBody = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Typ een bericht...", fontSize = (18 * zoom).sp, fontWeight = FontWeight.Bold) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                val interactionSourceSend = remember { MutableInteractionSource() }
                val isPressedSend by interactionSourceSend.collectIsPressedAsState()
                val scaleSend by animateFloatAsState(
                    targetValue = if (isPressedSend) 0.88f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "scale"
                )
                Surface(
                    onClick = {
                        if (newMessageBody.isNotBlank()) {
                            messagesVm.sendMessage(address, newMessageBody)
                            newMessageBody = ""
                        }
                    },
                    interactionSource = interactionSourceSend,
                    modifier = Modifier.size(56.dp).scale(scaleSend),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: SmsMessage, zoom: Float) {
    val isSent = message.isSent
    val bubbleColor = if (isSent) Color(0xFF6366F1) else Color(0xFF1E293B)
    val textColor = Color.White
    val borderColor = if (isSent) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isSent) Alignment.End else Alignment.Start) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 40.dp, topEnd = 40.dp,
                bottomStart = if (isSent) 40.dp else 8.dp,
                bottomEnd = if (isSent) 8.dp else 40.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.body,
                modifier = Modifier.padding(18.dp),
                fontSize = (20 * zoom).sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                lineHeight = (26 * zoom).sp
            )
        }
        Text(
            text = formatTime(message.timestamp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun NewMessageScreen(onContactSelected: (String, String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var contacts by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<Pair<String, String>>()
            context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")?.use { cursor ->
                val nIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val pIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nIdx) ?: "Onbekend"
                    val phone = cursor.getString(pIdx) ?: ""
                    list.add(name to phone)
                }
            }
            contacts = list.distinctBy { it.second.replace(" ", "") }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Zoek contact...", fontWeight = FontWeight.Black) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(40.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(contacts.filter { it.first.contains(searchQuery, true) || it.second.contains(searchQuery) }) { contact ->
                val interactionSourceItem = remember { MutableInteractionSource() }
                val isPressedItem by interactionSourceItem.collectIsPressedAsState()
                val scaleItem by animateFloatAsState(
                    targetValue = if (isPressedItem) 0.96f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "scale"
                )
                Surface(
                    onClick = { onContactSelected(contact.first, contact.second) },
                    interactionSource = interactionSourceItem,
                    shape = RoundedCornerShape(40.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth().scale(scaleItem)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Text(contact.first.take(1).uppercase(), fontWeight = FontWeight.Black, fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(contact.first, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            Text(contact.second, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestView(onGrant: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.Lock, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(24.dp))
            Text("Berichten Beveiligd", fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Geef toestemming om uw berichten veilig te tonen.", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp), fontWeight = FontWeight.Bold)
            Button(onClick = onGrant, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(40.dp)) {
                Text("TOESTEMMING GEVEN", fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun DefaultSmsBanner(onSet: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f), 
        shape = RoundedCornerShape(40.dp), 
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Stel in als standaard SMS-app", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onSet, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(16.dp)) {
                Text("FIX NU", fontWeight = FontWeight.Black)
            }
        }
    }
}

fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
