package com.seniorenlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigButton(
    emoji: String, 
    label: String, 
    color: Color, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier, 
    onLongClick: (() -> Unit)? = null,
    badge: Int? = null, 
    small: Boolean = false,
    fontSizeMultiplier: Float = 1f
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "s")
    
    // Adjust height and padding based on font size multiplier to prevent clipping
    val adjustedModifier = if (fontSizeMultiplier > 1.2f) {
        modifier.heightIn(min = (if (small) 120.dp else 160.dp) * (fontSizeMultiplier / 1.2f))
    } else {
        modifier.heightIn(min = if (small) 85.dp else 100.dp)
    }

    Box(
        modifier = adjustedModifier
            .scale(scale)
            .shadow(if (pressed) 2.dp else 6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.65f))))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(if (small) 2.dp else 4.dp), 
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)
        ) {
            Text(
                emoji, 
                fontSize = (if (small) 22.sp else 32.sp) * fontSizeMultiplier,
                lineHeight = (if (small) 24.sp else 34.sp) * fontSizeMultiplier
            )
            Spacer(Modifier.height(if (small) 1.dp else 2.dp))
            Text(
                label, 
                fontSize = (if (small) 12.sp else 14.sp) * fontSizeMultiplier, 
                fontWeight = FontWeight.Bold,
                color = Color.White, 
                textAlign = TextAlign.Center, 
                maxLines = if (fontSizeMultiplier > 1.5f) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = (if (small) 14.sp else 16.sp) * fontSizeMultiplier
            )
        }
        if (badge != null && badge > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size((24 * fontSizeMultiplier).coerceIn(24f, 44f).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)), 
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.toString(), 
                    fontSize = (12 * fontSizeMultiplier).coerceIn(12f, 22f).sp,
                    fontWeight = FontWeight.Bold, 
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SOSButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("\uD83C\uDD98", fontSize = 32.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                "SOS NOODGEVAL", 
                fontSize = 22.sp, 
                fontWeight = FontWeight.ExtraBold,
                color = Color.White, 
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    val fontSizeMultiplier = 1.0f // Normally you'd pass this, but let's keep headers readable
    
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(16.dp))
        Text(
            title, 
            fontSize = 26.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
