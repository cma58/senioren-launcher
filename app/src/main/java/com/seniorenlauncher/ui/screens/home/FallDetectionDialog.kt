package com.seniorenlauncher.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

import androidx.compose.ui.tooling.preview.Preview
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

@Composable
fun FallDetectionDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var timeLeft by remember { mutableIntStateOf(30) }

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
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "VAL GEDETECTEERD!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    "Bent u gevallen? We gaan hulp inschakelen over:",
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Text(
                    "$timeLeft",
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("IK MAAK HET GOED (STOP)", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("BEL NU HULP", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
