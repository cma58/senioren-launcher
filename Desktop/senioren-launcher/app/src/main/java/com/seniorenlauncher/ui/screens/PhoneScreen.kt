package com.seniorenlauncher.ui.screens
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.components.ScreenHeader

data class QuickDial(val name:String,val phone:String,val emoji:String,val color:Color)
val CONTACTS=listOf(QuickDial("Mama","+32474123456","👩‍🦳",Color(0xFFE53E3E)),QuickDial("Papa","+32475234567","👴",Color(0xFF3B82F6)),QuickDial("Zoon","+32476345678","👨",Color(0xFF38A169)),QuickDial("Dochter","+32477456789","👩",Color(0xFF8B5CF6)),QuickDial("Dokter","+32478567890","⚕️",Color(0xFFF59E0B)),QuickDial("Buur","+32479678901","🏠",Color(0xFF14B8A6)))

@Composable fun PhoneScreen(onNav:(String)->Unit,onBack:()->Unit){
    val ctx=LocalContext.current;var num by remember{mutableStateOf("")}
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Bellen",onBack)
        LazyVerticalGrid(GridCells.Fixed(3),Modifier.height(150.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            items(CONTACTS){c->Column(Modifier.clip(RoundedCornerShape(12.dp)).background(c.color.copy(0.15f)).clickable{dial(ctx,c.phone)}.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Text(c.emoji,fontSize=26.sp);Text(c.name,fontSize=12.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onBackground)}}
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(14.dp),contentAlignment=Alignment.Center){
            Text(if(num.isEmpty())"Nummer invoeren" else num,fontSize=if(num.isEmpty())18.sp else 28.sp,fontWeight=FontWeight.Bold,color=if(num.isEmpty())MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,letterSpacing=2.sp)}
        Spacer(Modifier.height(6.dp))
        LazyVerticalGrid(GridCells.Fixed(3),Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
            items(listOf("1","2","3","4","5","6","7","8","9","*","0","#")){d->
                Box(Modifier.height(52.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).clickable{num+=d},contentAlignment=Alignment.Center){
                    Text(d,fontSize=26.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)}}}
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFEF4444).copy(0.8f)).clickable{num=num.dropLast(1)},contentAlignment=Alignment.Center){Text("⌫ Wissen",fontSize=15.sp,fontWeight=FontWeight.Bold,color=Color.White)}
            Box(Modifier.weight(1.5f).height(50.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF38A169).copy(0.9f)).clickable{if(num.isNotEmpty())dial(ctx,num)},contentAlignment=Alignment.Center){Text("📞 Bellen",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color.White)}
        }
        Spacer(Modifier.height(4.dp))
    }
}
private fun dial(ctx:Context,num:String){try{ctx.startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:$num")))}catch(_:Exception){}}
