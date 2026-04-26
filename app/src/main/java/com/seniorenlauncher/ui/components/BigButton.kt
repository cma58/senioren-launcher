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
    weatherText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "s")
    
    val buttonHeight = if (small) 100.dp else 140.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
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
            Box(contentAlignment = Alignment.Center) {
                if (icon != null) {
                    Image(
                        painter = rememberAsyncImagePainter(icon),
                        contentDescription = null,
                        modifier = Modifier.size(if (small) 36.dp else 48.dp),
                        contentScale = ContentScale.Fit
                    )
                } else if (emoji != null) {
                    Text(
                        emoji, 
                        fontSize = if (small) 24.sp else 36.sp,
                        lineHeight = if (small) 28.sp else 40.sp
                    )
                }
                
                // Weather overlay (Temperature)
                if (weatherText != null) {
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 8.dp, y = 8.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 2.dp)
                    ) {
                        Text(
                            weatherText, 
                            color = Color.White, 
                            fontSize = 12.sp, 
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                label, 
                fontSize = if (small) 14.sp else 18.sp, 
                fontWeight = FontWeight.Bold,
                color = Color.White, 
                textAlign = TextAlign.Center, 
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (small) 16.sp else 22.sp
            )
        }
        
        if (badge != null && badge > 0) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 2.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)), 
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.toString(), 
                    fontSize = 14.sp,
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
            .wrapContentHeight()
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
            .heightIn(min = 80.dp)
            .padding(vertical = 12.dp), 
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
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold, 
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
