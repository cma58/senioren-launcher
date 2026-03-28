package com.seniorenlauncher.ui.screens
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

// Dit is jouw telefoonboek data
data class Contact(val name: String, val phone: String, val emoji: String, val color: Color)
val CONTACTS = listOf(
    Contact("Mama", "+32 474 12 34 56", "👩‍🦳", Color(0xFF38A169)),
    Contact("Zoon", "+32 476 34 56 78", "👨", Color(0xFF3B82F6)),
    Contact("Dochter", "+32 477 45 67 89", "👩", Color(0xFFF59E0B))
)

@Composable fun ContactsScreen(onCall:(String)->Unit,onBack:()->Unit){
    val ctx=LocalContext.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Contacten",onBack)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.weight(1f)){
            items(CONTACTS){c->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.color.copy(0.1f)).clickable{try{ctx.startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:${c.phone}")))}catch(_:Exception){}}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                Box(Modifier.size(56.dp).clip(CircleShape).background(c.color.copy(0.7f)),contentAlignment=Alignment.Center){Text(c.emoji,fontSize=28.sp)}
                Spacer(Modifier.width(16.dp))
                Column{Text(c.name,fontSize=22.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onBackground);Text(c.phone,fontSize=15.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF38A169).copy(0.8f)),contentAlignment=Alignment.Center){Text("📞",fontSize=22.sp)}
            }}
        }
    }
}
