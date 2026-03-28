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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.ui.components.ScreenHeader
@Composable fun SettingsScreen(vm:SettingsViewModel,onBack:()->Unit){
    val s by vm.settings.collectAsState()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(12.dp,8.dp)){
        ScreenHeader("Instellingen",onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Sec("🎨 Thema"){Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){AppTheme.entries.forEach{t->OutlinedButton({vm.updateTheme(t)},Modifier.weight(1f)){Text(when(t){AppTheme.CLASSIC->"Klassiek";AppTheme.HIGH_CONTRAST->"Contrast";AppTheme.LIGHT->"Licht"},fontSize=12.sp)}}}}
            Sec("📐 Layout"){Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){LayoutType.entries.forEach{l->OutlinedButton({vm.updateLayout(l)},Modifier.weight(1f)){Text(when(l){LayoutType.GRID_2x3->"2×3";LayoutType.GRID_3x4->"3×4";LayoutType.GRID_1x1->"1×1"},fontSize=13.sp)}}}}
            Sec("🔤 Lettergrootte"){Slider(value=s.fontSize.toFloat(),onValueChange={vm.updateFontSize(it.toInt())},valueRange=14f..24f);Text("Voorbeeld — ${s.fontSize}px",fontSize=s.fontSize.sp,color=MaterialTheme.colorScheme.onSurface)}
            Sec("🛡️ Veiligheid"){Tog("Valdetectie",s.fallDetectionEnabled){vm.toggleFall()};Tog("Batterij-SMS",s.batteryAlertEnabled){vm.toggleBattery()};Tog("Nachtmodus",s.nightModeAuto){vm.toggleNight()}}
            Sec("ℹ️ Over"){Text("Senioren Launcher v1.0\nOpen Source · GPL-3.0\nGeen tracking · Geen advertenties",fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
        }
    }
}
@Composable fun Sec(t:String,c:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp)){Column(Modifier.padding(14.dp)){Text(t,fontSize=15.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));c()}}}
@Composable fun Tog(l:String,v:Boolean,f:()->Unit){Row(Modifier.fillMaxWidth().padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){Text(l,fontSize=14.sp,color=MaterialTheme.colorScheme.onSurface);Switch(v,{f()})}}
