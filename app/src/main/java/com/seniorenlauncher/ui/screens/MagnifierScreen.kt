package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun MagnifierScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        ScreenHeader(title = "Vergrootglas", onBack = onBack)
        
        if (hasCameraPermission) {
            MagnifierCameraPreview(modifier = Modifier.weight(1f))
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Camera toegang nodig voor het vergrootglas.", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

@Composable
fun MagnifierCameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var zoomState by remember { mutableFloatStateOf(0f) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )
                        cameraControl = camera.cameraControl
                    } catch (exc: Exception) {
                        Log.e("Magnifier", "Use case binding failed", exc)
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Zoom Controls Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(32.dp))
                Slider(
                    value = zoomState,
                    onValueChange = { 
                        zoomState = it
                        cameraControl?.setLinearZoom(it)
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.Gray
                    )
                )
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Flash Button
                LargeCircleButton(
                    icon = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    onClick = {
                        isFlashOn = !isFlashOn
                        cameraControl?.enableTorch(isFlashOn)
                    },
                    color = if (isFlashOn) Color(0xFFFBBF24) else Color.DarkGray
                )
                
                // Freeze Button (simulated)
                LargeCircleButton(
                    icon = Icons.Default.Camera,
                    onClick = {
                        Toast.makeText(context, "Beeld vastgezet (niet echt)", Toast.LENGTH_SHORT).show()
                    },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun LargeCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, color: Color) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = color,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(36.dp), tint = Color.White)
        }
    }
}
