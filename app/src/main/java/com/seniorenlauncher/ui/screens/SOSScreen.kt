package com.seniorenlauncher.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.QuickContact
import com.seniorenlauncher.service.SOSService
import com.seniorenlauncher.ui.components.ScreenHeader
import kotlinx.coroutines.delay

@Composable
fun SOSScreen(onBack: () -> Unit) {
    // --- DEMO MODE TOGGLE ---
    val isDemoMode = false

    val context = LocalContext.current
    val dao = LauncherApp.instance.database.contactDao()
    val realSosContacts by dao.getSosContacts().collectAsState(initial = emptyList())
    
    // Samsung fix: Controleer of de app bovenop andere apps mag verschijnen
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- DUMMY DATA ---
    val dummySosContacts = listOf(
        QuickContact(name = "Dochter Sofie", phoneNumber = "06 12345678", isSosContact = true),
        QuickContact(name = "Buurman Jan", phoneNumber = "06 87654321", isSosContact = true)
    )

    val sosContacts = if (isDemoMode) dummySosContacts else realSosContacts

    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    var sosTriggered by remember { mutableStateOf(false) }

    // Check permissions and GPS status
    var hasLocationPermission by remember { mutableStateOf(
        if (isDemoMode) true else ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    )}
    
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    var isGpsEnabled by remember { mutableStateOf(if (isDemoMode) true else locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) }

    // Launcher for GPS resolution
    val gpsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isGpsEnabled = true
        }
    }

    // Re-check periodically
    LaunchedEffect(isDemoMode) {
        if (!isDemoMode) {
            while (true) {
                hasLocationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                delay(2000)
            }
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            val duration = 3000f
            while (isHolding && holdProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed / duration).coerceAtMost(1f)
                delay(16)
            }
            if (holdProgress >= 1f) {
                sosTriggered = true
                if (!isDemoMode) triggerSOS(context)
                else Toast.makeText(context, "Demo Mode: SOS geactiveerd", Toast.LENGTH_SHORT).show()
            }
        } else {
            holdProgress = 0f
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0F172A) // Slate 900
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            ScreenHeader(title = "SOS Noodhulp", onBack = onBack)
            
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!sosTriggered) {
                    Spacer(Modifier.height(12.dp))

                    // --- SAMSUNG LOCKSCREEN FIX ---
                    if (!canDrawOverlays) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            },
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(40.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        ) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(16.dp))
                                Text(
                                    "Samsung blokkeert hulp. Tik hier en zet 'Verschijnen bovenop' AAN.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    if (!hasLocationPermission || !isGpsEnabled) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(40.dp),
                            border = BorderStroke(2.dp, Color(0xFFF59E0B))
                        ) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (!hasLocationPermission) "Locatie nodig" else "GPS staat uit",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        "Klik hier om te herstellen.",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (!hasLocationPermission) {
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = android.net.Uri.fromParts("package", context.packageName, null)
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                                            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                                            val client = LocationServices.getSettingsClient(context)
                                            val task = client.checkLocationSettings(builder.build())
                                            
                                            task.addOnFailureListener { exception ->
                                                if (exception is ResolvableApiException) {
                                                    try {
                                                        val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                                                        gpsLauncher.launch(intentSenderRequest)
                                                    } catch (sendEx: IntentSender.SendIntentException) {
                                                        Log.e("SOS", "Error sending resolution", sendEx)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("HERSTEL", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    Text(
                        "HOU DE KNOP 3 SECONDEN IN\nOM HULP TE ROEPEN",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 32.sp,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                    
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 20.dp)) {
                        CircularProgressIndicator(
                            progress = { holdProgress },
                            modifier = Modifier.size(300.dp),
                            color = Color(0xFFEF4444),
                            strokeWidth = 14.dp,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            strokeCap = StrokeCap.Round
                        )
                        
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        LaunchedEffect(isPressed) { isHolding = isPressed }
                        
                        val scale by animateFloatAsState(
                            if (isPressed) 0.88f else 1f, 
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "scale"
                        )

                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                                    )
                                )
                                .border(8.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable(interactionSource = interactionSource, indication = null) {},
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SOS", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("HULP", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Text(
                        "Uw noodcontacten ontvangen direct\nuw locatie via SMS.",
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    SOSActiveUI(onBack)
                }
                
                Spacer(Modifier.height(40.dp))
                
                if (sosContacts.isNotEmpty()) {
                    Text(
                        "Contactpersonen:", 
                        fontWeight = FontWeight.Black, 
                        fontSize = 18.sp, 
                        color = Color(0xFF6366F1), // Indigo accent
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        sosContacts.forEach { contact ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(40.dp),
                                color = Color(0xFF1E293B).copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(48.dp).background(Color(0xFF6366F1).copy(alpha = 0.2f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("👤", fontSize = 24.sp)
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(contact.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        Text(contact.phoneNumber, fontSize = 16.sp, color = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(40.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Let op: Geen noodcontacten ingesteld!\nKlik op de knop in instellingen.",
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                            fontSize = 18.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SOSActiveUI(onBack: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFFEF4444).copy(alpha = 0.2f * alpha), CircleShape)
                .border(2.dp, Color(0xFFEF4444).copy(alpha = alpha), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Warning, 
                null, 
                modifier = Modifier.size(80.dp), 
                tint = Color(0xFFEF4444)
            )
        }
        
        Spacer(Modifier.height(40.dp))
        
        Text(
            "NOODSIGNAAL\nVERSTUURD!", 
            fontSize = 32.sp, 
            fontWeight = FontWeight.Black, 
            color = Color(0xFFEF4444), 
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            "Blijf rustig. Uw locatie is verzonden naar uw contactpersonen.", 
            fontSize = 20.sp, 
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center, 
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(Modifier.height(60.dp))
        
        Surface(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(90.dp),
            shape = RoundedCornerShape(40.dp),
            color = Color(0xFF10B981), // Emerald for safety
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("IK BEN VEILIG", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

fun triggerSOS(context: Context) {
    val intent = Intent(context, SOSService::class.java)
    context.startForegroundService(intent)
}
