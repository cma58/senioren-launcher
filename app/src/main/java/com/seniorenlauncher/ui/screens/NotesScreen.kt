package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.Note
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotesScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val dao = LauncherApp.instance.database.noteDao()
    val notes by dao.getAll().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Notities", onBack = onBack)
        
        if (notes.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📝", fontSize = 80.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Nog geen notities", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Druk op de + knop om te beginnen", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onClick = { noteToEdit = note },
                        onDelete = { scope.launch { dao.delete(note) } }
                    )
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LargeFloatingActionButton(
            onClick = {
                noteToEdit = null
                showAddDialog = true
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Add, "Nieuwe notitie", modifier = Modifier.size(40.dp))
        }
    }

    if (showAddDialog || noteToEdit != null) {
        NoteDialog(
            note = noteToEdit,
            onDismiss = { 
                showAddDialog = false
                noteToEdit = null
            },
            onSave = { title, content ->
                scope.launch {
                    val newNote = noteToEdit?.copy(title = title, content = content, updatedAt = System.currentTimeMillis())
                        ?: Note(title = title, content = content)
                    dao.insert(newNote)
                }
                showAddDialog = false
                noteToEdit = null
            }
        )
    }
}

@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateStr = SimpleDateFormat("d MMMM HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(note.title.ifBlank { "Geen titel" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(note.content, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(dateStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Verwijder", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun NoteDialog(note: Note?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (note == null) "Nieuwe Notitie" else "Notitie Bewerken", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Uw tekst...") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    minLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (content.isNotBlank() || title.isNotBlank()) onSave(title, content) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Opslaan", fontSize = 18.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren", fontSize = 18.sp)
            }
        }
    )
}
