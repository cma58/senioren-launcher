package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable fun CallingScreen(onEnd:()->Unit){
    var sec by remember{mutableIntStateOf(0)}
    LaunchedEffect(Unit){while(true){delay(1000);sec++}}
    Column(Modifier.fillMaxSize().background(Color(0xFF38A169).copy(0.05f)),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        Box(Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF38A169).copy(0.7f)),contentAlignment=Alignment.Center){Text("👩‍🦳",fontSize=52.sp)}
        Spacer(Modifier.height(16.dp))
        Text("Mama",fontSize=32.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Verbonden • %02d:%02d".format(sec/60,sec%60),fontSize=18.sp,color=Color(0xFF38A169))
        Spacer(Modifier.height(48.dp))
        Box(Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFEF4444)).clickable{onEnd()},contentAlignment=Alignment.Center){Text("📵",fontSize=36.sp)}
        Spacer(Modifier.height(8.dp))
        Text("Ophangen",fontSize=14.sp,color=Color(0xFFEF4444))
    }
}
