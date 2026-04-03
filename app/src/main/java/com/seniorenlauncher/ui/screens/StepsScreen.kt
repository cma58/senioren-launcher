package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.seniorenlauncher.ui.components.ScreenHeader

@Composable
fun StepsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    var hasPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Stappen", onBack = onBack)
        
        if (hasPermission) {
            StepCounterContent()
        } else {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Toegang tot bewegingsgegevens is nodig om uw stappen te tellen.", textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) }) {
                        Text("Toestaan")
                    }
                }
            }
        }
    }
}

@Composable
fun StepCounterContent() {
    val context = LocalContext.current
    var stepsSinceBoot by remember { mutableFloatStateOf(0f) }
    var initialSteps by remember { mutableFloatStateOf(-1f) }
    
    val currentSteps = if (initialSteps == -1f) 0 else (stepsSinceBoot - initialSteps).toInt()

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    val count = event.values[0]
                    stepsSinceBoot = count
                    if (initialSteps == -1f) {
                        initialSteps = count
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Grote stappen cirkel
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(280.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(8.dp, MaterialTheme.colorScheme.primary)
            ) {}
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.DirectionsWalk, null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = currentSteps.toString(),
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "STAPPEN VANDAAG",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
        
        // Info kaarten
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Afstand",
                value = String.format("%.1f", currentSteps * 0.00075) + " km",
                color = Color(0xFF3B82F6)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Calorieën",
                value = (currentSteps * 0.04).toInt().toString() + " kcal",
                color = Color(0xFFEF4444)
            )
        }
        
        Spacer(Modifier.height(32.dp))
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Text(
                    "Blijven bewegen is belangrijk voor uw gezondheid! Probeer elke dag een klein stukje te wandelen.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 14.sp, color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
