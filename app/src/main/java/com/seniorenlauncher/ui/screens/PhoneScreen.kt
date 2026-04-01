package com.seniorenlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class DeviceContact(val name: String, val number: String, val photoUri: String? = null)

@Composable
fun PhoneScreen(onNavigate: (String) -> Unit, onBack: () -> Unit, settingsVm: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val settings by settingsVm.settings.collectAsState()
    val fontSizeMultiplier = settings.fontSize / 16f
    
    val dao = LauncherApp.instance.database.contactDao()
    val favorieten by dao.getAll().collectAsState(initial = emptyList())
    
    var phoneNumber by remember { mutableStateOf("") }
    var showContacts by remember { mutableStateOf(false) }
    
    // Zoek contact op basis van huidig nummer
    val matchedContact = favorieten.find { it.phoneNumber.replace(" ", "") == phoneNumber.replace(" ", "") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ScreenHeader(
            title = if (showContacts) "Kies Contact" else "Telefoon", 
            onBack = { if (showContacts) showContacts = false else onBack() }
        )
        
        if (showContacts) {
            AllContactsList(fontSizeMultiplier, favorieten) { selectedNumber ->
                phoneNumber = selectedNumber
                showContacts = false
            }
        } else {
            DialerContent(
                phoneNumber = phoneNumber,
                fontSizeMultiplier = fontSizeMultiplier,
                matchedContact = matchedContact,
                favorieten = favorieten,
                onNumberChange = { phoneNumber = it },
                onShowContacts = { showContacts = true },
                onCall = { makeDirectCall(context, phoneNumber) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColumnScope.DialerContent(
    phoneNumber: String,
    fontSizeMultiplier: Float,
    matchedContact: QuickContact?,
    favorieten: List<QuickContact>,
    onNumberChange: (String) -> Unit,
    onShowContacts: () -> Unit,
    onCall: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.contactDao()

    // --- DE "WIE BEL IK?" KAART ---
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp * fontSizeMultiplier.coerceAtLeast(1f))
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (matchedContact != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (matchedContact != null) {
                    if (matchedContact.photoUri != null) {
                        AsyncImage(
                            model = matchedContact.photoUri,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(matchedContact.emoji, fontSize = 40.sp)
                    }
                    Text(
                        matchedContact.name, 
                        fontSize = 32.sp * fontSizeMultiplier, 
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = phoneNumber.ifEmpty { "Toets nummer..." },
                    fontSize = (if (matchedContact != null) 24f else 42f).sp * fontSizeMultiplier,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (phoneNumber.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (phoneNumber.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            onClick = { 
                                vibratePhone(context, 50)
                                onNumberChange(phoneNumber.dropLast(1)) 
                            },
                            onLongClick = { 
                                vibratePhone(context, 100)
                                onNumberChange("") 
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Backspace, "Wis", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // --- SNELKIEZERS ---
    if (phoneNumber.isEmpty() && favorieten.isNotEmpty()) {
        LazyRow(
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorieten) { contact ->
                Column(
                    Modifier
                        .width(100.dp * fontSizeMultiplier.coerceAtLeast(1f))
                        .clip(RoundedCornerShape(16.dp))
                        .combinedClickable(
                            onClick = { 
                                vibratePhone(context, 50)
                                makeDirectCall(context, contact.phoneNumber) 
                            },
                            onLongClick = {
                                vibratePhone(context, 100)
                                scope.launch {
                                    dao.delete(contact)
                                    Toast.makeText(context, "${contact.name} verwijderd", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    if (contact.photoUri != null) {
                        AsyncImage(
                            model = contact.photoUri,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(contact.emoji, fontSize = 32.sp)
                    }
                    Text(
                        contact.name, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // --- TOETSENBORD ---
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(keys) { key ->
            Surface(
                onClick = { 
                    vibratePhone(context, 40)
                    if (phoneNumber.length < 15) onNumberChange(phoneNumber + key) 
                },
                modifier = Modifier.aspectRatio(1.3f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(key, fontSize = 40.sp * fontSizeMultiplier, fontWeight = FontWeight.Black)
                }
            }
        }
    }

    // --- BELKNOP ---
    Button(
        onClick = { 
            vibratePhone(context, 60)
            if (phoneNumber.isNotEmpty()) onCall()
            else onShowContacts()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp * fontSizeMultiplier.coerceAtLeast(1f))
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38A169)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(Icons.Default.Call, null, modifier = Modifier.size(36.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            if (phoneNumber.isNotEmpty()) "BELLEN" else "ALLE CONTACTEN", 
            fontSize = 24.sp * fontSizeMultiplier, 
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun AllContactsList(
    fontSizeMultiplier: Float, 
    favorieten: List<QuickContact>,
    onContactSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.contactDao()
    var contacts by remember { mutableStateOf<List<DeviceContact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        contacts = fetchAllDeviceContacts(context)
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isEmpty()) contacts
        else contacts.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.number.contains(searchQuery) 
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            placeholder = { Text("Zoek contact...", fontSize = 18.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        if (filteredContacts.isEmpty() && searchQuery.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Geen contacten gevonden voor '$searchQuery'", color = Color.Gray)
            }
        }

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredContacts) { contact ->
                val normalizedNumber = contact.number.replace(" ", "")
                val existingFavorites = favorieten.filter { it.phoneNumber.replace(" ", "") == normalizedNumber }
                val isFavorite = existingFavorites.isNotEmpty()

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { 
                        vibratePhone(context, 40)
                        onContactSelected(contact.number) 
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (contact.photoUri != null) {
                            AsyncImage(
                                model = contact.photoUri,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.name.take(1).uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(contact.name, fontSize = 20.sp * fontSizeMultiplier, fontWeight = FontWeight.Bold)
                            Text(contact.number, fontSize = 16.sp * fontSizeMultiplier, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        IconButton(onClick = {
                            vibratePhone(context, 60)
                            scope.launch {
                                if (isFavorite) {
                                    existingFavorites.forEach { dao.delete(it) }
                                    Toast.makeText(context, "${contact.name} verwijderd uit favorieten", Toast.LENGTH_SHORT).show()
                                } else {
                                    dao.insert(QuickContact(
                                        name = contact.name, 
                                        phoneNumber = contact.number, 
                                        photoUri = contact.photoUri
                                    ))
                                    Toast.makeText(context, "${contact.name} toegevoegd aan favorieten", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder, 
                                contentDescription = "Favoriet", 
                                tint = if (isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

fun vibratePhone(context: Context, durationMillis: Long) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(durationMillis)
    }
}

fun fetchAllDeviceContacts(context: Context): List<DeviceContact> {
    val list = mutableListOf<DeviceContact>()
    try {
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, 
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx)
                val number = it.getString(numIdx)
                val photo = it.getString(photoIdx)
                if (name != null && number != null) list.add(DeviceContact(name, number, photo))
            }
        }
    } catch (e: Exception) { }
    return list.distinctBy { it.number.replace(" ", "") }
}

fun makeDirectCall(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(dialIntent)
    }
}
