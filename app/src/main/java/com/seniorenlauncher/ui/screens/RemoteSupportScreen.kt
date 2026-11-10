package com.seniorenlauncher.ui.screens
import com.seniorenlauncher.R
import com.seniorenlauncher.util.i18n

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.ui.components.ScreenHeader
import com.seniorenlauncher.util.AppLauncher

@Composable
fun RemoteSupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ScreenHeader(title = i18n(R.string.hulp_op_afstand), onBack = onBack)
        
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("👨‍🔧", fontSize = 60.sp)
            Text(
                i18n(R.string.hoe_werkt_het),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // --- STAPPENPLAN ---
            SupportStep(1, i18n(R.string.druk_op_de_grote_knop))
            SupportStep(2, i18n(R.string.tik_in_die_app_onderaan))
            SupportStep(3, i18n(R.string.druk_bovenin_op_de_blauwe))
            SupportStep(4, i18n(R.string.kies_voor_hele_scherm_delen))
            SupportStep(5, i18n(R.string.vertel_de_id_en_het))
            SupportStep(6, i18n(R.string.druk_op_accepteren_of_toestaan))

            Spacer(Modifier.height(32.dp))

            // --- DE ACTIEKNOP ---
            Button(
                onClick = { AppLauncher.startRemoteSupport(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A5568))
            ) {
                Icon(Icons.Default.Launch, null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text(i18n(R.string.open_hulp_app), fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Extra info kaart
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        i18n(R.string.u_kunt_de_hulp_app),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SupportStep(number: Int, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
