package com.seniorenlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "scale"
    )
    
    val buttonHeight = if (small) 120.dp else 168.dp
    val cornerRadius = 40.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .scale(scale)
            .shadow(
                elevation = if (pressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = color.copy(alpha = 0.3f),
                spotColor = color
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.85f),
                        color.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // High-end glass shine effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 400f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
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
                        fontSize = if (small) 40.sp else 56.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.25f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 4f),
                                blurRadius = 12f
                            )
                        )
                    )
                }
                
                if (weatherText != null) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 10.dp, y = 10.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            weatherText, 
                            color = Color.White, 
                            fontSize = 15.sp, 
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(14.dp))
            
            Text(
                label.uppercase(), 
                fontSize = if (small) 15.sp else 19.sp, 
                fontWeight = FontWeight.Black,
                color = Color.White, 
                textAlign = TextAlign.Center, 
                maxLines = 1,
                letterSpacing = 1.2.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        if (badge != null && badge > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .size(34.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(2.dp, color.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        badge.toString(), 
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black, 
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp, top = 8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onBack,
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF1E293B), // Slate 800
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "←", 
                        fontSize = 44.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.width(24.dp))
            Text(
                title, 
                fontSize = 34.sp,
                fontWeight = FontWeight.Black, 
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                lineHeight = 42.sp
            )
        }
    }
}
