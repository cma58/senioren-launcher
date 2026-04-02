package com.seniorenlauncher.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import androidx.lifecycle.viewmodel.compose.viewModel

data class Conversation(
    val address: String,
    val snippet: String,
    val date: Long,
    val contactName: String? = null,
    val threadId: Long = 0,
    val isRead: Boolean = true
)

data class SmsMessage(
    val body: String,
    val date: Long,
    val isMe: Boolean
)

@Composable
fun MessagesScreen(
    onBack: () -> Unit, 
    settingsVm: SettingsViewModel = viewModel(),
    messagesVm: MessagesViewModel = viewModel(),
    initialAddress: String? = null,
    initialName: String? = null
) {
    val settings by settingsVm.settings.collectAsState()
    val fontSizeMultiplier = settings.fontSize / 16f
    
    val dao = LauncherApp.instance.database.contactDao()
    val favorieten by dao.getAll().collectAsState(initial = emptyList())
    
    var selectedAddress by remember { mutableStateOf<String?>(initialAddress) }
    var selectedName by remember { mutableStateOf<String?>(initialName) }
    var showNewMessagePicker by remember { mutableStateOf(false) }
    
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ScreenHeader(
            title = when {
                showNewMessagePicker -> "Kies Contact"
                selectedAddress == null -> "Berichten"
                else -> selectedName ?: selectedAddress!!
            }, 
            onBack = {
                when {
                    showNewMessagePicker -> showNewMessagePicker = false
                    selectedAddress != null -> {
                        selectedAddress = null
                        selectedName = null
                    }
                    else -> onBack()
                }
            }
        )
        
        Box(Modifier.weight(1f)) {
            if (showNewMessagePicker) {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(favorieten) { contact ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedAddress = contact.phoneNumber
                                selectedName = contact.name
                                showNewMessagePicker = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(contact.emoji, fontSize = 28.sp)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(contact.name, fontSize = 20.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold)
                                    Text(contact.phoneNumber, fontSize = 16.sp * fontSizeMultiplier)
                                }
                            }
                        }
                    }
                }
            } else if (selectedAddress == null) {
                ConversationList(messagesVm, fontSizeMultiplier) { conv ->
                    selectedAddress = conv.address
                    selectedName = conv.contactName
                }
                
                FloatingActionButton(
                    onClick = { showNewMessagePicker = true },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, "Nieuw bericht")
                }
            } else {
                ChatScreen(selectedAddress!!, messagesVm, fontSizeMultiplier)
            }
        }
    }
}

@Composable
fun ConversationList(
    messagesVm: MessagesViewModel,
    fontSizeMultiplier: Float, 
    onConversationClick: (Conversation) -> Unit
) {
    val context = LocalContext.current
    val conversations by messagesVm.conversations.collectAsState()
    val isLoading by messagesVm.isLoading.collectAsState()
    val searchQuery by messagesVm.searchQuery.collectAsState()

    LaunchedEffect(Unit) {
        messagesVm.loadConversations(context)
    }

    Column {
        // --- Zoekbalk ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { messagesVm.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            placeholder = { Text("Zoek in berichten...", fontSize = 18.sp * fontSizeMultiplier) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                { IconButton(onClick = { messagesVm.setSearchQuery("") }) { Icon(Icons.Default.Clear, null) } }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (isLoading && conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (searchQuery.isEmpty()) "Geen berichten gevonden" else "Niets gevonden voor '$searchQuery'", fontSize = 18.sp * fontSizeMultiplier)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(conversations) { conv ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onConversationClick(conv) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (conv.isRead) MaterialTheme.colorScheme.surfaceVariant 
                                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        border = if (!conv.isRead) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(50.dp).clip(CircleShape).background(
                                    if (conv.isRead) MaterialTheme.colorScheme.secondaryContainer 
                                    else MaterialTheme.colorScheme.primary
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (conv.contactName ?: conv.address).take(1).uppercase(), 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 22.sp * fontSizeMultiplier,
                                    color = if (conv.isRead) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = conv.contactName ?: conv.address, 
                                        fontSize = 18.sp * fontSizeMultiplier, 
                                        fontWeight = if (conv.isRead) FontWeight.Bold else FontWeight.ExtraBold
                                    )
                                    if (!conv.isRead) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                    }
                                }
                                Text(
                                    text = conv.snippet, 
                                    fontSize = 16.sp * fontSizeMultiplier, 
                                    maxLines = 1, 
                                    overflow = TextOverflow.Ellipsis, 
                                    color = if (conv.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (conv.isRead) FontWeight.Normal else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(address: String, messagesVm: MessagesViewModel, fontSizeMultiplier: Float) {
    val context = LocalContext.current
    val messages by messagesVm.messages.collectAsState()
    val msgFontSizeScale by messagesVm.messageFontSizeMultiplier.collectAsState()
    var newMessage by remember { mutableStateOf("") }
    val scrollState = rememberLazyListState()

    LaunchedEffect(address) {
        messagesVm.loadMessages(context, address)
    }

    Column(Modifier.fillMaxSize()) {
        // --- Tekstgrootte Knoppen ---
        Row(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tekstgrootte:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { messagesVm.adjustFontSize(-0.2f) },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) { Icon(Icons.Default.Remove, "Kleiner") }
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { messagesVm.adjustFontSize(0.2f) },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) { Icon(Icons.Default.Add, "Groter") }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            state = scrollState,
            reverseLayout = true,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg, fontSizeMultiplier * msgFontSizeScale)
            }
        }

        Row(
            Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Typ een bericht...", fontSize = 16.sp * fontSizeMultiplier) },
                shape = RoundedCornerShape(24.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 18.sp * fontSizeMultiplier)
            )
            FloatingActionButton(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        messagesVm.sendMessage(context, address, newMessage)
                        newMessage = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Send, null)
            }
        }
    }
}

@Composable
fun MessageBubble(message: SmsMessage, totalFontSizeMultiplier: Float) {
    val alignment = if (message.isMe) Alignment.End else Alignment.Start
    val color = if (message.isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = RoundedCornerShape(
                topStart = 16.dp, 
                topEnd = 16.dp, 
                bottomStart = if (message.isMe) 16.dp else 0.dp, 
                bottomEnd = if (message.isMe) 0.dp else 16.dp
            ),
            modifier = Modifier.padding(horizontal = 8.dp).widthIn(max = 300.dp)
        ) {
            Text(
                text = message.body,
                modifier = Modifier.padding(12.dp),
                fontSize = 18.sp * totalFontSizeMultiplier,
                color = if (message.isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                lineHeight = (24.sp * totalFontSizeMultiplier)
            )
        }
    }
}
