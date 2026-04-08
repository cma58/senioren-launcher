package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SosContactSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.contactDao()
    val sosContacts by dao.getSosContacts().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "SOS Instellingen", onBack = onBack)
        
        Text(
            "Deze mensen krijgen een SMS met uw locatie bij nood.",
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp, end = 8.dp),
            fontSize = 18.sp,
            lineHeight = 24.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (sosContacts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5))
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚠️ Geen contacten", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC53030))
                            Spacer(Modifier.height(8.dp))
                            Text("Voeg minimaal één persoon toe die we kunnen waarschuwen.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
            
            items(sosContacts) { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFCA5A5))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).background(Color(0xFFEF4444), CircleShape), contentAlignment = Alignment.Center) {
                            Text(contact.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(contact.name, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text(contact.phoneNumber, fontSize = 16.sp, color = Color.DarkGray)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                dao.delete(contact) // Verwijder volledig uit SOS lijst
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Verwijder", tint = Color(0xFFC53030), modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().height(90.dp).padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Text("IEMAND TOEVOEGEN", fontSize = 22.sp, fontWeight = FontWeight.Black)
        }
    }

    if (showAddDialog) {
        SosContactPickerDialog(
            onDismiss = { showAddDialog = false },
            onContactSelected = { name, phone ->
                scope.launch {
                    dao.insert(QuickContact(
                        name = name, 
                        phoneNumber = phone, 
                        isSosContact = true,
                        emoji = "🆘",
                        color = 0xFFDC2626
                    ))
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun SosContactPickerDialog(onDismiss: () -> Unit, onContactSelected: (String, String) -> Unit) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) onDismiss()
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            withContext(Dispatchers.IO) {
                val list = mutableListOf<Pair<String, String>>()
                val cursor = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                    null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )
                cursor?.use {
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = it.getString(nameIdx)
                        val num = it.getString(numIdx)
                        if (name != null && num != null) list.add(name to num)
                    }
                }
                contacts = list.distinctBy { it.second.replace(" ", "").replace("-", "") }
                isLoading = false
            }
        }
    }

    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().fillMaxHeight(0.9f), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Wie wilt u toevoegen?", fontSize = 24.sp, fontWeight = FontWeight.Black)
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek op naam...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // Knop voor nieuw contact
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            type = ContactsContract.Contacts.CONTENT_TYPE
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("NIEUW CONTACT MAKEN", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredContacts) { contact: Pair<String, String> ->
                            Card(
                                onClick = { onContactSelected(contact.first, contact.second) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(contact.first, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text(contact.second, fontSize = 14.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) {
                    Text("ANNULEREN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
