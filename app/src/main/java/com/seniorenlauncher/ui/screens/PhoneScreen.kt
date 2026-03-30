package com.seniorenlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class PhoneContact(val name: String, val number: String)

@Composable
fun PhoneScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, settingsVm: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by settingsVm.settings.collectAsState()
    val fontSizeMultiplier = settings.fontSize / 16f
    
    val dao = LauncherApp.instance.database.contactDao()
    val localContacts by dao.getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    
    var phoneNumber by remember { mutableStateOf("") }
    var showContacts by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ScreenHeader(title = if (showContacts) "Contacten" else "Bellen", onBack = {
            if (showContacts) showContacts = false else onBack()
        })
        
        if (showContacts) {
            ContactsPicker(
                fontSizeMultiplier = fontSizeMultiplier,
                localContacts = localContacts,
                onContactSelected = {
                    phoneNumber = it.number
                    showContacts = false
                },
                onAddToFavorites = { contact ->
                    scope.launch {
                        dao.insert(QuickContact(name = contact.name, phoneNumber = contact.number))
                        Toast.makeText(context, "${contact.name} toegevoegd aan favorieten", Toast.LENGTH_SHORT).show()
                    }
                },
                onSmsSelected = { contact ->
                    // Navigate to SMS with this contact pre-selected
                    // We need a way to pass the contact to MessagesScreen
                    // For now, let's use a simple navigation with a trick or update MessagesScreen
                    onNavigate("sms?address=${contact.number}&name=${contact.name}")
                }
            )
        } else {
            DialerContent(
                phoneNumber = phoneNumber,
                fontSizeMultiplier = fontSizeMultiplier,
                localContacts = localContacts,
                onNumberChange = { phoneNumber = it },
                onShowContacts = { showContacts = true },
                onNavigate = onNavigate,
                onCall = { makeDirectCall(context, phoneNumber) },
                onAddContact = { showAddDialog = true },
                onSms = { onNavigate("sms?address=$phoneNumber") }
            )
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone ->
                scope.launch {
                    dao.insert(QuickContact(name = name, phoneNumber = phone))
                }
                phoneNumber = phone
                showAddDialog = false
            },
            initialPhone = phoneNumber
        )
    }
}

@Composable
fun ColumnScope.DialerContent(
    phoneNumber: String,
    fontSizeMultiplier: Float,
    localContacts: List<QuickContact>,
    onNumberChange: (String) -> Unit,
    onShowContacts: () -> Unit,
    onNavigate: (String) -> Unit,
    onCall: () -> Unit,
    onAddContact: () -> Unit,
    onSms: () -> Unit
) {
    val context = LocalContext.current
    
    // --- Suggestions Area ---
    if (phoneNumber.length >= 2) {
        val deviceContacts = remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
        LaunchedEffect(Unit) {
            deviceContacts.value = fetchDeviceContacts(context)
        }
        
        val suggestions = (localContacts.map { PhoneContact(it.name, it.phoneNumber) } + deviceContacts.value)
            .filter { it.number.replace(" ", "").contains(phoneNumber) || it.name.contains(phoneNumber, ignoreCase = true) }
            .distinctBy { it.number.replace(" ", "") }
            .take(5)

        if (suggestions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(suggestions) { contact ->
                    SuggestionChip(contact, fontSizeMultiplier) {
                        onNumberChange(contact.number)
                    }
                }
            }
        }
    }

    // --- Display Area ---
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = phoneNumber.ifEmpty { "Voer nummer in" },
                    fontSize = (32 * fontSizeMultiplier).coerceIn(24f, 48f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (phoneNumber.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                
                // Show contact name if recognized
                val matchedContact = localContacts.find { it.phoneNumber.replace(" ", "") == phoneNumber.replace(" ", "") }
                if (matchedContact != null) {
                    Text(matchedContact.name, fontSize = 16.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                } else if (phoneNumber.isNotEmpty() && phoneNumber.length > 5) {
                    // Show "Add Contact" button if not empty and not recognized
                    TextButton(onClick = onAddContact) {
                        Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Opslaan als contact", fontSize = 14.sp * fontSizeMultiplier)
                    }
                }
            }
            
            if (phoneNumber.isNotEmpty()) {
                IconButton(onClick = { onNumberChange(phoneNumber.dropLast(1)) }) {
                    Icon(Icons.Default.Backspace, "Delete", modifier = Modifier.size(32.dp))
                }
            }
        }
    }

    // --- Tabs / Quick Access ---
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PhoneTabButton(
            icon = Icons.Default.Star,
            label = "Favorieten",
            modifier = Modifier.weight(1f),
            onClick = { onNavigate("contacts") },
            fontSizeMultiplier = fontSizeMultiplier
        )
        PhoneTabButton(
            icon = Icons.Default.ContactPhone,
            label = "Contacten",
            modifier = Modifier.weight(1f),
            onClick = onShowContacts,
            fontSizeMultiplier = fontSizeMultiplier
        )
    }

    Spacer(Modifier.height(8.dp))

    // --- Dial Pad ---
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        items(keys) { key ->
            DialButton(
                text = key,
                onClick = { if (phoneNumber.length < 15) onNumberChange(phoneNumber + key) },
                fontSizeMultiplier = fontSizeMultiplier
            )
        }
    }

    // --- Call & SMS Buttons ---
    Row(Modifier.fillMaxWidth().height(80.dp * fontSizeMultiplier.coerceAtLeast(1f)), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { 
                if (phoneNumber.isNotEmpty()) onCall()
                else Toast.makeText(context, "Voer eerst een nummer in", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38A169)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Call, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
            Text("BELLEN", fontSize = 20.sp * fontSizeMultiplier, fontWeight = FontWeight.ExtraBold)
        }
        
        Button(
            onClick = { 
                if (phoneNumber.isNotEmpty()) onSms()
                else Toast.makeText(context, "Voer eerst een nummer in", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.weight(0.7f).fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Sms, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(8.dp))
            Text("SMS", fontSize = 20.sp * fontSizeMultiplier, fontWeight = FontWeight.ExtraBold)
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun SuggestionChip(contact: PhoneContact, fontSizeMultiplier: Float, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(contact.name, fontSize = 14.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(contact.number, fontSize = 11.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ColumnScope.ContactsPicker(
    fontSizeMultiplier: Float, 
    localContacts: List<QuickContact>,
    onContactSelected: (PhoneContact) -> Unit,
    onAddToFavorites: (PhoneContact) -> Unit,
    onSmsSelected: (PhoneContact) -> Unit
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        contacts = fetchDeviceContacts(context)
        isLoading = false
    }

    val filteredContacts = contacts.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.number.contains(searchQuery)
    }

    Column(Modifier.weight(1f)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = { Text("Zoek contact...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(12.dp)
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts) { contact ->
                    val isFavorite = localContacts.any { it.phoneNumber.replace(" ", "") == contact.number.replace(" ", "") }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(Modifier.weight(1f).clickable { onContactSelected(contact) }, verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.name.take(1).uppercase(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(contact.name, fontSize = 18.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(contact.number, fontSize = 14.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            IconButton(onClick = { onSmsSelected(contact) }) {
                                Icon(Icons.Default.Sms, contentDescription = "SMS", tint = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
                            }
                            
                            IconButton(onClick = { if (!isFavorite) onAddToFavorites(contact) }) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                                    contentDescription = "Favoriet",
                                    tint = if (isFavorite) Color(0xFFF59E0B) else Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun fetchDeviceContacts(context: Context): List<PhoneContact> {
    val contactList = mutableListOf<PhoneContact>()
    try {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, 
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            
            while (it.moveToNext()) {
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                if (name != null && number != null) {
                    contactList.add(PhoneContact(name, number))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return contactList.distinctBy { it.number.replace(Regex("[^0-9+]"), "") }
}

@Composable
fun DialButton(text: String, onClick: () -> Unit, fontSizeMultiplier: Float) {
    Surface(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1.2f),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = (36 * fontSizeMultiplier).coerceIn(28f, 50f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun PhoneTabButton(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit, fontSizeMultiplier: Float) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp * fontSizeMultiplier.coerceAtLeast(1f)),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 16.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddContactDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit, initialPhone: String = "") {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialPhone) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nieuw Contact", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Naam") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Telefoonnummer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, Modifier.weight(1f)) {
                        Text("Annuleren", fontSize = 18.sp)
                    }
                    Button(
                        onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Opslaan", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}

fun makeDirectCall(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Kan niet bellen: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
