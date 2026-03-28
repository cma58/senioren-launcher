package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.seniorenlauncher.ui.components.ScreenHeader

data class QuickDialContact(val name: String, val phone: String, val emoji: String, val color: Color)

val QUICK_CONTACTS = listOf(
    QuickDialContact("Mama", "+32474123456", "👩‍🦳", Color(0xFFE53E3E)),
    QuickDialContact("Papa", "+32475234567", "👴", Color(0xFF3B82F6)),
    QuickDialContact("Zoon", "+32476345678", "👨", Color(0xFF38A169)),
    QuickDialContact("Dochter", "+32477456789", "👩", Color(0xFF8B5CF6)),
    QuickDialContact("Dokter", "+32478567890", "⚕️", Color(0xFFF59E0B)),
    QuickDialContact("Buur", "+32479678901", "🏠", Color(0xFF14B8A6)),
)

@Composable
fun PhoneScreen(onNav: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var number by remember { mutableStateOf("") }
    val digits = listOf("1","2","3","4","5","6","7","8","9","*","0","#")

    Column(
        Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp, 8.dp)
    ) {
        ScreenHeader("Bellen", onBack)

        // Snelkeuze contacten
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(160.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(QUICK_CONTACTS) { contact ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(contact.color.copy(alpha = 0.15f))
                        .clickable { makeCall(context, contact.phone) }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(contact.emoji, fontSize = 28.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        contact.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Nummer display
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (number.isEmpty()) "Nummer invoeren" else number,
                fontSize = if (number.isEmpty()) 18.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (number.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant
                       else MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // Nummertoetsen
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(digits) { digit ->
                Box(
                    Modifier.height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { number += digit },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        digit,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Actie knoppen
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Wissen
            Box(
                Modifier.weight(1f).height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.8f))
                    .clickable { number = number.dropLast(1) },
                contentAlignment = Alignment.Center
            ) {
                Text("⌫ Wissen", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Bellen
            Box(
                Modifier.weight(1.5f).height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF38A169).copy(alpha = 0.9f))
                    .clickable { if (number.isNotEmpty()) makeCall(context, number) },
                contentAlignment = Alignment.Center
            ) {
                Text("📞 Bellen", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun makeCall(context: Context, number: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {
        data = Uri.parse("tel:$number")
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
        == PackageManager.PERMISSION_GRANTED) {
        context.startActivity(intent)
    } else {
        // Fallback: open dialer zonder direct te bellen
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$number")
        }
        context.startActivity(dialIntent)
    }
}
