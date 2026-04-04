package com.seniorenlauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun ContactsScreen(onBack: () -> Unit, settingsVm: SettingsViewModel = viewModel()) {
    // --- DEMO MODE UIT ---
    val isDemoMode = false

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember { LauncherApp.instance.database.contactDao() }
    
    // Bestaande echte data uit de database
    val realContacts by dao.getAll().collectAsState(initial = emptyList())
    
    val contacts = realContacts
    
    val settings by settingsVm.settings.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        ScreenHeader(title = "Contacten", onBack = onBack)
        
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Mijn Lijst", 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.heightIn(min = 48.dp).wrapContentHeight(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("Nieuw", fontSize = 16.sp)
            }
        }

        if (contacts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Geen favoriete contacten.\nTik op 'Nieuw' om er een toe te voegen.",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(contacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onCall = { makeCall(context, contact.phoneNumber) },
                        onToggleFavorite = {
                            scope.launch {
                                dao.insert(contact.copy(isSosContact = !contact.isSosContact))
                            }
                        },
                        onDelete = {
                            scope.launch {
                                dao.delete(contact)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone ->
                scope.launch {
                    dao.insert(QuickContact(name = name, phoneNumber = phone))
                    showAddDialog = false
                }
            }
        )
    }
}

private fun makeCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Kon niet bellen", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ContactItem(
    contact: QuickContact, 
    onCall: () -> Unit, 
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isSosContact) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(contact.color).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(contact.emoji, fontSize = 28.sp)
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(Modifier.weight(1f)) {
                Text(
                    contact.name, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    contact.phoneNumber, 
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = Color.Gray)
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (contact.isSosContact) Icons.Default.Star else Icons.Default.StarOutline,
                    contentDescription = "Favoriet",
                    tint = if (contact.isSosContact) Color(0xFFD97706) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }

            Surface(
                onClick = onCall,
                shape = CircleShape,
                color = Color(0xFF38A169),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
fun AddContactDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Opslaan", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
