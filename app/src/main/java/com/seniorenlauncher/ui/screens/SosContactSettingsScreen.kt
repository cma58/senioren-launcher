package com.seniorenlauncher.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch

@Composable
fun SosContactSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.contactDao()
    val allContacts by dao.getAll().collectAsState(initial = emptyList())
    val sosContacts by dao.getSosContacts().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "SOS Contacten", onBack = onBack)
        
        Text(
            "Deze personen ontvangen een SMS met uw locatie als u de SOS-knop gebruikt.",
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sosContacts.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Nog geen noodcontacten.", color = Color.Gray)
                    }
                }
            }
            
            items(sosContacts) { contact ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFEF4444))
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(contact.phoneNumber, fontSize = 14.sp)
                        }
                        IconButton(onClick = {
                            scope.launch {
                                dao.insert(contact.copy(isSosContact = false))
                            }
                        }) {
                            Icon(Icons.Default.Delete, "Verwijder", tint = Color.Gray)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth().height(70.dp).padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("CONTACT TOEVOEGEN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showAddDialog) {
        val availableToPromotion = allContacts.filter { !it.isSosContact }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Kies uit favorieten") },
            text = {
                if (availableToPromotion.isEmpty()) {
                    Text("Voeg eerst contacten toe aan uw favorieten in het belscherm.")
                } else {
                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        items(availableToPromotion) { contact ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            dao.insert(contact.copy(isSosContact = true))
                                            showAddDialog = false
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, null)
                                Spacer(Modifier.width(12.dp))
                                Text(contact.name, fontSize = 18.sp)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Annuleren") }
            }
        )
    }
}
