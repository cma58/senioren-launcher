package com.seniorenlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigButton(
    emoji: String? = null,
    icon: Drawable? = null,
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
    
    // Dynamic height calculation to prevent text overlap
    val minHeight = if (small) 90.dp else 120.dp
    val adjustedHeight = (minHeight * fontSizeMultiplier).coerceIn(minHeight, 300.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = adjustedHeight)
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
            .padding(8.dp), 
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Image(
                    painter = rememberAsyncImagePainter(icon),
                    contentDescription = null,
                    modifier = Modifier.size((if (small) 36.dp else 48.dp) * fontSizeMultiplier.coerceIn(1f, 1.5f)),
                    contentScale = ContentScale.Fit
                )
            } else if (emoji != null) {
                Text(
                    emoji, 
                    fontSize = (if (small) 24.sp else 36.sp) * fontSizeMultiplier.coerceIn(1f, 1.5f),
                    lineHeight = (if (small) 28.sp else 40.sp) * fontSizeMultiplier.coerceIn(1f, 1.5f)
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                label, 
                fontSize = (if (small) 14.sp else 18.sp) * fontSizeMultiplier, 
                fontWeight = FontWeight.Bold,
                color = Color.White, 
                textAlign = TextAlign.Center, 
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = (if (small) 16.sp else 22.sp) * fontSizeMultiplier
            )
        }
        
        if (badge != null && badge > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size((28 * fontSizeMultiplier).coerceIn(28f, 50f).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)), 
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.toString(), 
                    fontSize = (14 * fontSizeMultiplier).coerceIn(14f, 24f).sp,
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
            .heightIn(min = 100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🆘", fontSize = 40.sp)
            Spacer(Modifier.width(16.dp))
            Text(
                "SOS NOOD", 
                fontSize = 28.sp, 
                fontWeight = FontWeight.ExtraBold,
                color = Color.White, 
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text("←", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(16.dp))
        Text(
            title, 
            fontSize = 30.sp, 
            fontWeight = FontWeight.ExtraBold, 
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
