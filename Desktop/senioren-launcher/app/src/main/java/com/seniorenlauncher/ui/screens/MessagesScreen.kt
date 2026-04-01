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

data class Msg(val from:String,val text:String,val time:String,val emoji:String,val unread:Boolean)
val MSGS=listOf(Msg("Mama","Hoe gaat het schat? Bel me!","10:30","👩‍🦳",true),Msg("Zoon","Ik kom zondag langs","11:15","👨",true),Msg("Dokter","Afspraak morgen 14:00","09:00","⚕️",false),Msg("Dochter","Foto's gestuurd!","16:45","👩",false))

@Composable fun MessagesScreen(onBack:()->Unit){
    var sel by remember{mutableStateOf<Msg?>(null)}
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader(if(sel!=null)sel!!.from else "Berichten",if(sel!=null){{sel=null}}else onBack)
        if(sel!=null){
            Column(Modifier.weight(1f).padding(8.dp)){
                Box(Modifier.clip(RoundedCornerShape(14.dp,14.dp,14.dp,4.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(14.dp)){
                    Column{Text(sel!!.text,fontSize=18.sp,color=MaterialTheme.colorScheme.onSurface);Text(sel!!.time,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
            }
            var reply by remember{mutableStateOf("")}
            Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                OutlinedTextField(reply,{reply=it},Modifier.weight(1f),placeholder={Text("Type een bericht...")})
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF3B82F6)).clickable{reply=""},contentAlignment=Alignment.Center){Text("📤",fontSize=20.sp)}
            }
        }else{
            LazyColumn(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(6.dp)){
                items(MSGS){m->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).clickable{sel=m}.padding(14.dp),verticalAlignment=Alignment.CenterVertically){
                    Text(m.emoji,fontSize=32.sp);Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(m.from,fontSize=17.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface);Text(m.time,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};Text(m.text,fontSize=14.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)}
                    if(m.unread)Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0xFF3B82F6)))
                }}
            }
        }
    }
}
