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

@Composable fun FlashlightScreen(onBack:()->Unit){
    val ctx=LocalContext.current;var on by remember{mutableStateOf(false)}
    fun toggle(v:Boolean){try{val cm=ctx.getSystemService(Context.CAMERA_SERVICE)as CameraManager;cm.cameraIdList.firstOrNull()?.let{cm.setTorchMode(it,v)};on=v}catch(_:Exception){}}
    DisposableEffect(Unit){onDispose{try{val cm=ctx.getSystemService(Context.CAMERA_SERVICE)as CameraManager;cm.cameraIdList.firstOrNull()?.let{cm.setTorchMode(it,false)}}catch(_:Exception){}}}
    Column(Modifier.fillMaxSize().background(if(on)Color(0xFFFBBF24).copy(0.08f)else MaterialTheme.colorScheme.background).padding(12.dp,8.dp),horizontalAlignment=Alignment.CenterHorizontally){
        ScreenHeader("Zaklamp",onBack);Spacer(Modifier.weight(1f))
        Box(Modifier.size(180.dp).clip(CircleShape).background(if(on)Color(0xFFFBBF24)else MaterialTheme.colorScheme.surfaceVariant).clickable{toggle(!on)},contentAlignment=Alignment.Center){Text("🔦",fontSize=72.sp)}
        Spacer(Modifier.height(20.dp))
        Text(if(on)"ZAKLAMP AAN" else "ZAKLAMP UIT",fontSize=28.sp,fontWeight=FontWeight.ExtraBold,color=if(on)Color(0xFFFBBF24)else MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Tik op de knop",fontSize=16.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
        Spacer(Modifier.weight(1f))
    }
}
