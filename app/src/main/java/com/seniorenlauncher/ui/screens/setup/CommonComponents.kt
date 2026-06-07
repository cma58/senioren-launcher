package com.seniorenlauncher.ui.screens.setup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPoint(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(20.dp))
        Column {
            Text(title.uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
            Text(description, fontSize = 16.sp, color = Color(0xFFCBD5E1), lineHeight = 22.sp, fontWeight = FontWeight.Medium)
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(2.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(24.dp),
                color = color.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(40.dp), tint = color)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(title.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(description, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFFCBD5E1))
            }
        }
    }
}

@Composable
fun DefaultAppRow(title: String, description: String, isGranted: Boolean, icon: ImageVector, isSenior: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(enabled = !isGranted, interactionSource = interactionSource, indication = null) { onClick() },
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) Color(0xFF064E3B).copy(alpha = 0.3f) else Color(0xFF7F1D1D).copy(alpha = 0.3f)
        ),
        border = BorderStroke(2.dp, if (isGranted) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f))
    ) {
        Row(Modifier.padding(if (isSenior) 32.dp else 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                null, 
                modifier = Modifier.size(if (isSenior) 48.dp else 40.dp), 
                tint = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444)
            )
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title.uppercase(), 
                    fontSize = if (isSenior) 22.sp else 18.sp, 
                    fontWeight = FontWeight.Black, 
                    color = Color.White
                )
                Text(
                    description, 
                    fontSize = if (isSenior) 18.sp else 15.sp, 
                    color = Color(0xFFCBD5E1),
                    fontWeight = FontWeight.Medium
                )
            }
            Text(if (isGranted) "✅" else "❌", fontSize = if (isSenior) 32.sp else 24.sp)
        }
    }
}