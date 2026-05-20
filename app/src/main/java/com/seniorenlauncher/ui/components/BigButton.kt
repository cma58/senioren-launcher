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
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigButton(
    emoji: String? = null,
    icon: Drawable? = null,
    vectorIcon: ImageVector? = null,
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
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "s")
    
    val buttonHeight = if (small) 110.dp else 150.dp
    val cornerRadius = if (small) 24.dp else 32.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
            .shadow(if (pressed) 2.dp else 10.dp, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.95f),
                        color.copy(alpha = 0.75f)
                    )
                )
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp), 
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (vectorIcon != null) {
                    Icon(
                        imageVector = vectorIcon,
                        contentDescription = null,
                        modifier = Modifier.size(if (small) 44.dp else 60.dp),
                        tint = Color.White
                    )
                } else if (icon != null) {
                    Image(
                        painter = rememberAsyncImagePainter(icon),
                        contentDescription = null,
                        modifier = Modifier.size(if (small) 44.dp else 60.dp),
                        contentScale = ContentScale.Fit
                    )
                } else if (emoji != null) {
                    Text(
                        emoji, 
                        fontSize = if (small) 32.sp else 44.sp,
                        lineHeight = if (small) 36.sp else 48.sp
                    )
                }
                
                // Weather overlay (Temperature)
                if (weatherText != null) {
                    Surface(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 12.dp, y = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.5f)
                    ) {
                        Text(
                            weatherText, 
                            color = Color.White, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                label, 
                fontSize = if (small) 16.sp else 22.sp, 
                fontWeight = FontWeight.Black,
                color = Color.White, 
                textAlign = TextAlign.Center, 
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (badge != null && badge > 0) {
            Surface(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(36.dp),
                shape = CircleShape,
                color = Color(0xFFEF4444),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        badge.toString(), 
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black, 
                        color = Color.White
                    )
                }
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        color = Color.Transparent
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clickable { onBack() },
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "←", 
                        fontSize = 44.sp, 
                        fontWeight = FontWeight.Black, 
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Text(
                title, 
                fontSize = 36.sp,
                fontWeight = FontWeight.Black, 
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
