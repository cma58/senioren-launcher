package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CallingScreen(onEnd: () -> Unit) {
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { kotlinx.coroutines.delay(1000); seconds++ } }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("👩‍🦳", fontSize = 64.sp)
        Spacer(Modifier.height(12.dp))
        Text("Mama", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text("Verbonden • ${"%02d".format(seconds/60)}:${"%02d".format(seconds%60)}",
            fontSize = 16.sp, color = Color(0xFF38A169))
        Spacer(Modifier.height(40.dp))
        Button(onClick = onEnd, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.size(72.dp)) { Text("📵", fontSize = 28.sp) }
    }
}
