package com.seniorenlauncher.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class Med(val name:String,val dose:String,val time:String,val color:Color,var taken:Boolean)

@Composable fun MedicationScreen(onBack:()->Unit){
    val meds=remember{mutableStateListOf(Med("Metformine","500mg","08:00",Color(0xFF3B82F6),true),Med("Aspirine","100mg","08:00",Color(0xFFEF4444),true),Med("Vitamine D","1000 IE","12:00",Color(0xFFF59E0B),false),Med("Bloeddruk","5mg","20:00",Color(0xFF38A169),false))}
    val taken=meds.count{it.taken};val total=meds.size
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Medicijnen",onBack)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp)){
            Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Vandaag",fontSize=15.sp,fontWeight=FontWeight.Bold);Text("$taken/$total ingenomen",fontSize=15.sp,fontWeight=FontWeight.Bold,color=if(taken==total)Color(0xFF38A169)else Color(0xFFF59E0B))}
                Spacer(Modifier.height(6.dp));LinearProgressIndicator(progress={taken.toFloat()/total},Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),color=if(taken==total)Color(0xFF38A169)else Color(0xFF3B82F6))}}
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){
            items(meds.size){i->val m=meds[i]
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp)){
                    Column{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                        Column{Text("💊 ${m.name}",fontSize=18.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface);Text("${m.dose} — ${m.time}",fontSize=14.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if(m.taken)Color(0xFF38A169).copy(0.15f)else MaterialTheme.colorScheme.surfaceVariant).clickable{meds[i]=m.copy(taken=!m.taken)}.padding(12.dp),contentAlignment=Alignment.Center){
                        Text(if(m.taken)"✅ Ingenomen" else "⬜ Nog niet ingenomen",fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=if(m.taken)Color(0xFF38A169)else MaterialTheme.colorScheme.onSurface)}
                    }
                }
            }
        }
    }
}
