package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.components.ScreenHeader
@Composable fun NotesScreen(onBack:()->Unit){
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Notities",onBack)
        Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
            Text("📝",fontSize=64.sp)
            Spacer(Modifier.height(16.dp))
            Text("Notities",fontSize=28.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("Binnenkort beschikbaar",fontSize=16.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
        }
    }
}
