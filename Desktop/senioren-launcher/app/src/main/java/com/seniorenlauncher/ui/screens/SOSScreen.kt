package com.seniorenlauncher.ui.screens
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
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
import kotlinx.coroutines.delay

@Composable fun SOSScreen(onBack:()->Unit){
    val ctx=LocalContext.current;var phase by remember{mutableStateOf("countdown")};var cd by remember{mutableIntStateOf(5)}
    LaunchedEffect(phase){
        if(phase=="countdown"){
            try{(ctx.getSystemService(Context.VIBRATOR_SERVICE)as?Vibrator)?.vibrate(VibrationEffect.createWaveform(longArrayOf(0,300,200,300,200,300),-1))}catch(_:Exception){}
            while(cd>0){delay(1000);cd--};phase="sent"
        }
    }
    Column(Modifier.fillMaxSize().background(if(phase=="sent")Color(0xFF38A169).copy(0.1f)else Color(0xFFDC2626).copy(0.1f)).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
        if(phase=="countdown"){
            Box(Modifier.size(120.dp).clip(CircleShape).background(Color(0xFFDC2626)),contentAlignment=Alignment.Center){Text("🆘",fontSize=52.sp)}
            Spacer(Modifier.height(20.dp))
            Text("SOS wordt verstuurd...",fontSize=26.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.onBackground,textAlign=TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("$cd",fontSize=72.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFFDC2626))
            Text("seconden",fontSize=18.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable{onBack()},contentAlignment=Alignment.Center){
                Text("ANNULEREN",fontSize=18.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface,letterSpacing=2.sp)}
        }else{
            Box(Modifier.size(120.dp).clip(CircleShape).background(Color(0xFF38A169).copy(0.2f)),contentAlignment=Alignment.Center){Text("✅",fontSize=56.sp)}
            Spacer(Modifier.height(16.dp))
            Text("SOS Verstuurd!",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF38A169))
            Spacer(Modifier.height(8.dp))
            Text("GPS-locatie verzonden naar:",fontSize=16.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            listOf("Mama","Zoon","Dochter").forEach{n->
                Box(Modifier.fillMaxWidth().padding(vertical=3.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF38A169).copy(0.15f)).padding(12.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){Text("✓ ",color=Color(0xFF38A169));Text(n,fontSize=18.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onBackground)}}}
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF38A169)).clickable{onBack()},contentAlignment=Alignment.Center){
                Text("Terug naar Home",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color.White)}
        }
    }
}
