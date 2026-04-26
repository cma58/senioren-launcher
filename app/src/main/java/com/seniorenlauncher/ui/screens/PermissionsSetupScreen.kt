package com.seniorenlauncher.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class PermissionCategory(
    val title: String,
    val description: String,
    val privacyNote: String,
    val icon: ImageVector,
    val permissions: List<String>
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PermissionsSetupScreen(onNext: () -> Unit, isSenior: Boolean = false) {
    val context = LocalContext.current

    val categories = remember(isSenior) {
        val list = mutableListOf<PermissionCategory>()
        
        list.add(PermissionCategory(
            if (isSenior) "Hulp bij nood" else "Locatie", 
            if (isSenior) "Als u in nood bent, kan de app uw familie vertellen waar u bent." 
            else "Nodig voor de SOS-functie om uw locatie te bepalen.", 
            "Privacy: Uw locatiegegevens worden alleen lokaal verwerkt en uitsluitend bij een actieve SOS-melding verstuurd naar uw eigen noodcontacten. Wij slaan geen locatie-historie op.",
            Icons.Default.LocationOn, 
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        ))

        list.add(PermissionCategory(
            if (isSenior) "Bellen naar familie" else "Bellen", 
            if (isSenior) "Hiermee kunt u direct familie of de dokter bellen."
            else "Om direct vanuit de launcher te bellen.", 
            "Privacy: De app belt alleen wanneer u op een contact drukt.",
            Icons.Default.Phone, 
            listOf(Manifest.permission.CALL_PHONE)
        ))

        list.add(PermissionCategory(
            if (isSenior) "Berichten sturen" else "SMS", 
            if (isSenior) "Zodat de telefoon een noodbericht kan sturen als dat nodig is."
            else "Nodig voor SOS en batterijwaarschuwingen.", 
            "Privacy: We lezen geen andere berichten.",
            Icons.Default.Sms, 
            listOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
        ))

        list.add(PermissionCategory(
            if (isSenior) "Uw bekenden" else "Contacten", 
            if (isSenior) "Om de namen van uw familie in de telefoon te zetten."
            else "Om favorieten te kunnen beheren.", 
            "Privacy: Uw contacten worden nooit gedeeld.",
            Icons.Default.ContactPhone, 
            listOf(Manifest.permission.READ_CONTACTS)
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.add(PermissionCategory(
                "Gezondheid", 
                "Zodat de telefoon uw stapjes kan tellen.", 
                "Privacy: Deze gegevens blijven op uw telefoon.",
                Icons.AutoMirrored.Filled.DirectionsWalk, 
                listOf(Manifest.permission.ACTIVITY_RECOGNITION)
            ))
        }

        list.add(PermissionCategory(
            if (isSenior) "Het vergrootglas" else "Camera", 
            if (isSenior) "Zodat u kleine lettertjes makkelijk kunt lezen."
            else "Nodig voor het vergrootglas en de zaklamp.", 
            "Privacy: Er worden geen foto's op de achtergrond gemaakt.",
            Icons.Default.CameraAlt, 
            listOf(Manifest.permission.CAMERA)
        ))

        val storagePerms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 16+ en 14+ ondersteuning voor Photo Picker (geen permissie nodig voor picker, 
            // maar voor volledige galerij app-functies is READ_MEDIA_VISUAL_USER_SELECTED/READ_MEDIA_IMAGES nodig)
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        list.add(PermissionCategory(
            if (isSenior) "Uw Foto's" else "Foto's", 
            if (isSenior) "Zodat u de foto's van uw familie kunt bekijken."
            else "Nodig om de galerij te kunnen tonen.", 
            "Privacy: De app gebruikt de moderne Android Photo Picker. We hebben alleen toegang tot foto's die u zelf selecteert of de mappen die u expliciet vrijgeeft.",
            Icons.Default.PhotoLibrary, 
            storagePerms
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(PermissionCategory(
                if (isSenior) "Herinneringen" else "Meldingen", 
                if (isSenior) "Zodat u een seintje krijgt voor uw medicijnen."
                else "Nodig voor medicijnalarmen.", 
                "Privacy: Geen reclame, alleen belangrijke meldingen.",
                Icons.Default.Notifications, 
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            ))
        }

        list
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val currentCategory = categories.getOrNull(currentStep) ?: return onNext()

    var isGranted by remember(currentStep) {
        mutableStateOf(checkPermissions(context, currentCategory.permissions))
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        isGranted = results.values.all { it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Stap ${currentStep + 1} van ${categories.size}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LinearProgressIndicator(
            progress = { (currentStep + 1) / categories.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = currentCategory,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
            },
            label = "PermissionStep"
        ) { category ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = if (isSenior) "Icoon voor ${category.title}" else null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = category.title,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = category.description,
                    fontSize = if (isSenior) 24.sp else 22.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = if (isSenior) 32.sp else 30.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = if (isSenior) "Informatie over privacy" else "Privacy", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = category.privacyNote,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = if (isSenior) "Gelukt" else null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (isSenior) "Het is gelukt!" else "Toegang verleend", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                }
            }
        } else {
            Button(
                onClick = { launcher.launch(currentCategory.permissions.toTypedArray()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (isSenior) "JA, DAT IS GOED" else "GEEF TOEGANG", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            
            TextButton(
                onClick = { 
                    if (currentStep < categories.size - 1) currentStep++ else onNext() 
                },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(if (isSenior) "Nu even niet" else "Sla deze stap over", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (isGranted) {
            Button(
                onClick = { 
                    if (currentStep < categories.size - 1) currentStep++ else onNext() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (currentStep < categories.size - 1) "GA NAAR DE VOLGENDE" else "KLAAR!", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = if (isSenior) "Volgende stap" else null, modifier = Modifier.size(28.dp))
            }
        }
    }
}

private fun checkPermissions(context: Context, permissions: List<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}
