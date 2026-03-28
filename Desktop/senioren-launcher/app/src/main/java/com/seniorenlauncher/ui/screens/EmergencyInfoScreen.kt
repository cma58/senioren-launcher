package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.components.ScreenHeader
@Composable fun EmergencyInfoScreen(onBack:()->Unit){
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Noodinfo",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            InfoCard("🏥 Medisch",Color(0xFFEF4444),listOf("Bloedgroep" to "A+","Allergieën" to "Penicilline, noten","Aandoeningen" to "Diabetes type 2","Pacemaker" to "Nee"))
            InfoCard("💊 Medicijnen",Color(0xFF3B82F6),listOf("Metformine" to "500mg 2x/dag","Aspirine" to "100mg 1x/dag","Vitamine D" to "1000 IE 1x/dag"))
            InfoCard("📱 ICE Contacten",Color(0xFF38A169),listOf("Mama" to "+32 474 12 34 56","Zoon" to "+32 476 34 56 78","Dochter" to "+32 477 45 67 89"))
            InfoCard("🏠 Persoonlijk",Color(0xFFF59E0B),listOf("Naam" to "Jan Peeters","Geboren" to "15-03-1942","Adres" to "Marktstraat 15, Eeklo","Huisarts" to "Dr. Van Damme"))
        }
    }
}
@Composable fun InfoCard(title:String,color:Color,items:List<Pair<String,String>>){
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color.copy(0.08f)).padding(14.dp)){
        Column{Text(title,fontSize=16.sp,fontWeight=FontWeight.Bold,color=color);Spacer(Modifier.height(8.dp))
            items.forEach{(k,v)->Row(Modifier.fillMaxWidth().padding(vertical=4.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(k,fontSize=14.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(v,fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurface)}}
        }
    }
}
