package com.seniorenlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BigButton(emoji: String, label: String, color: Color, onClick: () -> Unit,
    modifier: Modifier = Modifier, badge: Int? = null, small: Boolean = false) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "s")
    Box(modifier = modifier.scale(scale).shadow(if (pressed) 2.dp else 6.dp, RoundedCornerShape(14.dp))
        .clip(RoundedCornerShape(14.dp))
        .background(Brush.linearGradient(listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.65f))))
        .pointerInput(Unit) { detectTapGestures(onPress = { pressed = true; tryAwaitRelease(); pressed = false; onClick() }) }
        .padding(if (small) 8.dp else 12.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = if (small) 26.sp else 32.sp)
            Spacer(Modifier.height(if (small) 2.dp else 4.dp))
            Text(label, fontSize = if (small) 12.sp else 14.sp, fontWeight = FontWeight.Bold,
                color = Color.White, textAlign = TextAlign.Center, maxLines = 1)
        }
        if (badge != null && badge > 0) {
            Box(Modifier.align(Alignment.TopEnd).size(20.dp).clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEF4444)), contentAlignment = Alignment.Center) {
                Text(badge.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SOSButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp))
        .background(Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
        .pointerInput(Unit) { detectTapGestures { onClick() } }, contentAlignment = Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83C\uDD98", fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Text("SOS NOODGEVAL", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White, letterSpacing = 3.sp)
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) { detectTapGestures { onBack() } }, contentAlignment = Alignment.Center) {
            Text("←", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
    }
}
