package com.seniorenlauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.seniorenlauncher.util.AppLauncher
import com.seniorenlauncher.util.InstalledApp

@Composable
fun AllAppsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val installedApps = remember { AppLauncher.getInstalledApps(context) }
    
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 12.dp, vertical = 8.dp)) {
        ScreenHeader(title = "Alle Apps", onBack = onBack)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(installedApps) { app ->
                AppItem(app) {
                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: InstalledApp, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // We use a placeholder for the icon since we don't have an easy way to show Drawable in Compose here without a wrapper
        // but for a senior launcher, maybe just the name and a generic icon or the first letter is fine, 
        // however, let's just use the name for now as the user asked for a way to see and open them.
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.name.take(1).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = app.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
