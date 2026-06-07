package com.seniorenlauncher.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.seniorenlauncher.util.PermissionUtils
import kotlinx.coroutines.delay

import androidx.compose.ui.tooling.preview.Preview
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

@Composable
fun FallDetectionDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(30) }
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasSosPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results.values.all { it }
    }

    if (!hasPermission) {
        PermissionRequestDialog(
            title = "SOS Toegang",
            description = "Om hulp te kunnen inschakelen bij een val hebben we toestemming nodig voor locatie, bellen en SMS.",
            onDismiss = onCancel,
            onGrant = {
                permissionLauncher.launch(PermissionUtils.getRequiredSosPermissions().toTypedArray())
            }
        )
        return
    }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onConfirm()
    }

    Dialog(
        onDismissRequest = { /* Geen dismiss door buiten klikken */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(40.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444)), // Red 500
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color.White
                )

                Text(
                    "VAL GEDETECTEERD!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    "Bent u gevallen? We gaan hulp inschakelen over:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    "$timeLeft",
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                val cancelInteractionSource = remember { MutableInteractionSource() }
                val cancelPressed by cancelInteractionSource.collectIsPressedAsState()
                val cancelScale by animateFloatAsState(
                    targetValue = if (cancelPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                Button(
                    onClick = onCancel,
                    interactionSource = cancelInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(90.dp).scale(cancelScale),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text("STOP / ANNULEER", fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                val confirmInteractionSource = remember { MutableInteractionSource() }
                val confirmPressed by confirmInteractionSource.collectIsPressedAsState()
                val confirmScale by animateFloatAsState(
                    targetValue = if (confirmPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                OutlinedButton(
                    onClick = onConfirm,
                    interactionSource = confirmInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(confirmScale),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text("BEL NU HULP", fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFallDetectionDialog() {
    SeniorenLauncherTheme {
        FallDetectionDialog(onConfirm = {}, onCancel = {})
    }
}
