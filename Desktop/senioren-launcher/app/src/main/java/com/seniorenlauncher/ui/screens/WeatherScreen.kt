package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.components.ScreenHeader
@Composable fun WeatherScreen(onBack:()->Unit){
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Weer",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally){
            Spacer(Modifier.height(16.dp));Text("🌤️",fontSize=64.sp);Text("18°C",fontSize=48.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.onBackground)
            Text("Gedeeltelijk bewolkt",fontSize=17.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF3B82F6).copy(0.1f)).padding(12.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){Text("🧥",fontSize=24.sp);Spacer(Modifier.width(8.dp));Column{Text("Kledingadvies",fontSize=14.sp,fontWeight=FontWeight.Bold,color=Color(0xFF3B82F6));Text("Een vest is voldoende",fontSize=14.sp,color=MaterialTheme.colorScheme.onSurface)}}}
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                listOf("💨" to "12 km/u","💧" to "65%","🌡️" to "16°C").forEach{(i,v)->Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(i,fontSize=22.sp);Text(v,fontSize=14.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)}}}}
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                listOf("Ma" to "☀️","Di" to "🌤️","Wo" to "🌧️","Do" to "⛅","Vr" to "🌤️").forEach{(d,i)->Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface).padding(8.dp)){Column(horizontalAlignment=Alignment.CenterHorizontally){Text(d,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(i,fontSize=22.sp)}}}}
        }
    }
}
