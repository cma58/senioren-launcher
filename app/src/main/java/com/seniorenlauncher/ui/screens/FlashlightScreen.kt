package com.seniorenlauncher.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun FlashlightScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.firstOrNull()
    
    var isFlashOn by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    // Zorg dat de zaklamp uitgaat als we het scherm verlaten
    DisposableEffect(Unit) {
        onDispose {
            if (isFlashOn && cameraId != null) {
                try { cameraManager.setTorchMode(cameraId, false) } catch (e: Exception) {}
            }
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Zaklamp", onBack = onBack)
        
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (isFlashOn) "De zaklamp is AAN" else "De zaklamp is UIT",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(Modifier.height(40.dp))
            
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(if (isFlashOn) Color(0xFFFBBF24) else Color.Gray)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (cameraId != null) {
                            isFlashOn = !isFlashOn
                            try {
                                cameraManager.setTorchMode(cameraId, isFlashOn)
                            } catch (e: Exception) {
                                isFlashOn = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isFlashOn) "💡" else "🔦",
                    fontSize = 80.sp
                )
            }
            
            Spacer(Modifier.height(40.dp))
            
            Button(
                onClick = {
                    if (cameraId != null) {
                        isFlashOn = !isFlashOn
                        cameraManager.setTorchMode(cameraId, isFlashOn)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFlashOn) Color(0xFFDC2626) else Color(0xFF38A169)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isFlashOn) "ZET UIT" else "ZET AAN",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
