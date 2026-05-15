package com.seniorenlauncher.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.seniorenlauncher.util.AppLauncher
import com.seniorenlauncher.util.InstalledApp

import androidx.compose.ui.tooling.preview.Preview
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

@Preview(showBackground = true)
@Composable
fun PreviewPinDialog() {
    SeniorenLauncherTheme {
        PinDialog(correctPin = "1234", onDismiss = {}, onSuccess = {})
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAppPickerDialog() {
    SeniorenLauncherTheme {
        AppPickerDialog(
            appId = "new",
            onDismiss = {},
            onAppsSelected = {},
            onRemove = {}
        )
    }
}

@Composable
fun PinDialog(
    correctPin: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(8.dp)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Beveiligde Instellingen", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    input.replace(Regex("."), "●").ifEmpty { " " },
                    fontSize = 24.sp,
                    letterSpacing = 8.sp,
                    color = if (error) Color.Red else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(Modifier.height(8.dp))
                
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rows.forEach { rowKeys ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowKeys.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> if (input.isNotEmpty()) input = input.dropLast(1)
                                            "OK" -> {
                                                if (input == correctPin) onSuccess() else {
                                                    error = true
                                                    input = ""
                                                }
                                            }
                                            else -> {
                                                if (input.length < 8) {
                                                    error = false
                                                    input += key
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 50.dp, max = 65.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(key, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppPickerDialog(
    appId: String,
    onDismiss: () -> Unit,
    onAppsSelected: (List<String>) -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val installedApps by produceState<List<InstalledApp>>(initialValue = emptyList(), context) {
        value = AppLauncher.getInstalledApps(context, includeIcons = true)
    }
    var searchQuery by remember { mutableStateOf("") }
    val selectedPackages = remember { mutableStateListOf<String>() }

    val filteredApps = remember(searchQuery, installedApps) {
        if (searchQuery.isEmpty()) installedApps
        else installedApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Kies Apps", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    if (appId.startsWith("mapped_")) {
                        TextButton(onClick = onRemove) {
                            Text("Verwijderen", color = Color.Red)
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek app...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))
                
                LazyColumn(Modifier.weight(1f)) {
                    items(filteredApps) { app ->
                        val isSelected = selectedPackages.contains(app.packageName)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    if (isSelected) selectedPackages.remove(app.packageName)
                                    else selectedPackages.add(app.packageName)
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(Modifier.width(8.dp))
                            if (app.icon != null) {
                                AsyncImage(
                                    model = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(app.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
                
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss, 
                        modifier = Modifier.weight(1f).heightIn(min = 60.dp).wrapContentHeight(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ANNULEREN")
                    }
                    Button(
                        onClick = { onAppsSelected(selectedPackages.toList()) }, 
                        modifier = Modifier.weight(1f).heightIn(min = 60.dp).wrapContentHeight(),
                        enabled = selectedPackages.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("VOEG TOE (${selectedPackages.size})")
                    }
                }
            }
        }
    }
}
