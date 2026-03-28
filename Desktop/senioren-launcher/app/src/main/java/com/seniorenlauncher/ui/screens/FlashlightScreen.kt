package com.seniorenlauncher.ui.screens

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun FlashlightScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var isOn by remember { mutableStateOf(false) }

    // Zaklamp aan/uit zetten
    fun toggleFlashlight(turnOn: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            cameraManager.setTorchMode(cameraId, turnOn)
            isOn = turnOn
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Zaklamp uit als scherm verlaten wordt
    DisposableEffect(Unit) {
        onDispose {
            try {
                val cm = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val id = cm.cameraIdList.firstOrNull()
                if (id != null) cm.setTorchMode(id, false)
            } catch (_: Exception) {}
        }
    }

    Column(
        Modifier.fillMaxSize()
            .background(
                if (isOn) Color(0xFFFBBF24).copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.background
            )
            .padding(12.dp, 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader("Zaklamp", onBack)

        Spacer(Modifier.weight(1f))

        // Grote aan/uit knop
        Box(
            Modifier.size(180.dp)
                .clip(CircleShape)
                .background(
                    if (isOn) Color(0xFFFBBF24)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable { toggleFlashlight(!isOn) },
            contentAlignment = Alignment.Center
        ) {
            Text("🔦", fontSize = 72.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            if (isOn) "ZAKLAMP AAN" else "ZAKLAMP UIT",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isOn) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onBackground,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Tik op de knop om de zaklamp\naan of uit te zetten",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.weight(1f))

        // Grote actieknop onderaan
        Box(
            Modifier.fillMaxWidth().height(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isOn) Color(0xFFEF4444).copy(alpha = 0.9f)
                    else Color(0xFFFBBF24).copy(alpha = 0.9f)
                )
                .clickable { toggleFlashlight(!isOn) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (isOn) "🔦  UIT ZETTEN" else "🔦  AAN ZETTEN",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isOn) Color.White else Color.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
