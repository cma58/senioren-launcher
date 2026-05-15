package com.seniorenlauncher.ui.screens.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPoint(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(description, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
        }
    }
}

@Composable
fun SetupOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(2.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(48.dp), tint = color)
            Spacer(Modifier.width(20.dp))
            Column {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
                Text(description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DefaultAppRow(title: String, description: String, isGranted: Boolean, icon: ImageVector, isSenior: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isGranted) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isGranted) Color(0xFFE8F5E9) else Color(0xFFFEF2F2)),
        border = BorderStroke(if (isSenior) 4.dp else 2.dp, if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444))
    ) {
        Row(Modifier.padding(if (isSenior) 24.dp else 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(if (isSenior) 40.dp else 32.dp), tint = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444))
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = if (isSenior) 22.sp else 18.sp, fontWeight = FontWeight.Bold, color = if (isGranted) Color(0xFF1B5E20) else Color(0xFF991B1B))
                Text(description, fontSize = if (isSenior) 18.sp else 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (isGranted) "✅" else "❌", fontSize = if (isSenior) 32.sp else 24.sp)
        }
    }
}