package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.content.ContextCompat
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun ContactsScreen(onCall: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp, 8.dp)
    ) {
        ScreenHeader("Contacten", onBack)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(QUICK_CONTACTS) { contact ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(contact.color.copy(alpha = 0.1f))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contact.phone}")
                            }
                            context.startActivity(intent)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar
                    Box(
                        Modifier.size(56.dp)
                            .clip(CircleShape)
                            .background(contact.color.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(contact.emoji, fontSize = 28.sp)
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            contact.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            contact.phone,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Bel-icoon
                    Box(
                        Modifier.size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38A169).copy(alpha = 0.8f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📞", fontSize = 22.sp)
                    }
                }
            }
        }
    }
}
