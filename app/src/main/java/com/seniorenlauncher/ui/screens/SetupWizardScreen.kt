package com.seniorenlauncher.ui.screens

import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.seniorenlauncher.LauncherApp
import com.seniorenlauncher.data.model.*
import com.seniorenlauncher.ui.screens.setup.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.stringResource
import com.seniorenlauncher.R

@Composable
fun SetupWizardScreen(
    onFinished: () -> Unit,
    settingsVm: SettingsViewModel
) {
    val settings by settingsVm.settings.collectAsState()
    
    var flow by remember { mutableStateOf(WizardSetupFlow.NONE) }
    var caregiverStep by remember { mutableIntStateOf(1) }
    var seniorStep by remember { mutableIntStateOf(1) }

    if (!settings.privacyAccepted) {
        PrivacyConsentScreen(onAccepted = { settingsVm.acceptPrivacy() })
        return
    }

    when (flow) {
        WizardSetupFlow.NONE -> {
            FlowSelectionScreen(onFlowSelected = { flow = it })
        }
        WizardSetupFlow.CAREGIVER -> {
            when (caregiverStep) {
                1 -> PermissionsSetupScreen(onNext = { caregiverStep = 2 }, isSenior = false)
                2 -> SystemPermissionsSetupScreen(onNext = { caregiverStep = 3 }, isSenior = false)
                3 -> DefaultAppsSetupScreen(onNext = { caregiverStep = 4 }, isSenior = false)
                4 -> SosSetupScreen(onNext = { caregiverStep = 5 }, settingsVm = settingsVm)
                5 -> FallDetectionSetupScreen(onNext = { caregiverStep = 6 }, settingsVm = settingsVm)
                6 -> SecuritySetupScreen(onNext = { caregiverStep = 7 }, settingsVm = settingsVm)
                7 -> HandoverScreen(onNext = { 
                    flow = WizardSetupFlow.SENIOR
                    seniorStep = 1
                })
                else -> {}
            }
        }
        WizardSetupFlow.SENIOR -> {
            val contactDao = remember { LauncherApp.instance.database.contactDao() }
            val sosCount by contactDao.getSosContacts().collectAsState(initial = emptyList())

            when (seniorStep) {
                1 -> SeniorWelcomeStep(onNext = { seniorStep = 5 })
                5 -> SeniorReadingStep(onNext = { seniorStep = 6 }, settingsVm = settingsVm)
                6 -> SeniorColorsStep(onNext = { seniorStep = 7 }, settingsVm = settingsVm)
                7 -> {
                    if (sosCount.isNotEmpty()) {
                        LaunchedEffect(Unit) {
                            settingsVm.completeSetup()
                            onFinished()
                        }
                    } else {
                        SeniorEmergencyStep(onNext = { 
                            settingsVm.completeSetup()
                            onFinished()
                        })
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun PrivacyConsentScreen(onAccepted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Slate 900
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        
        Icon(
            Icons.Default.PrivacyTip,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF3B82F6)
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.privacy_title).uppercase(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White,
            lineHeight = 40.sp
        )
        
        Spacer(Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate 800
            shape = RoundedCornerShape(40.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(Modifier.padding(24.dp)) {
                PrivacyPoint(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.privacy_promise_title),
                    description = stringResource(R.string.privacy_promise_desc)
                )
                
                Spacer(Modifier.height(20.dp))
                
                PrivacyPoint(
                    icon = Icons.Default.Gavel,
                    title = stringResource(R.string.privacy_processing_title),
                    description = stringResource(R.string.privacy_processing_desc)
                )
                
                Spacer(Modifier.height(20.dp))
                
                PrivacyPoint(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.privacy_rights_title),
                    description = stringResource(R.string.privacy_rights_desc)
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Door op de knop hieronder te drukken, gaat u akkoord met onze privacyverklaring.",
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.padding(horizontal = 16.dp),
            fontWeight = FontWeight.Medium
        )
        
        Spacer(Modifier.height(24.dp))
        
        val interactionSource = remember { MutableInteractionSource() }
        val pressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (pressed) 0.94f else 1f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
        )

        Button(
            onClick = onAccepted,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .scale(scale),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            Text(stringResource(R.string.privacy_accept).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun FlowSelectionScreen(onFlowSelected: (WizardSetupFlow) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Slate 900
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WELKOM.\nWIE STELT DEZE TELEFOON IN?",
            fontSize = 32.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate 800
            shape = RoundedCornerShape(40.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Text(
                "De setup bestaat uit twee delen: technische instellingen door de beheerder en visuele keuzes voor de senior.",
                modifier = Modifier.padding(24.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

        SetupOptionCard(
            title = "IK BEN DE MANTELZORGER",
            description = "Ik stel dit toestel technisch in (aanbevolen).",
            icon = Icons.Default.VolunteerActivism,
            color = Color(0xFF3B82F6),
            onClick = { onFlowSelected(WizardSetupFlow.CAREGIVER) }
        )

        Spacer(Modifier.height(24.dp))

        SetupOptionCard(
            title = "IK BEN DE GEBRUIKER",
            description = "Ik ga deze telefoon direct zelf gebruiken.",
            icon = Icons.Default.Person,
            color = Color(0xFF10B981),
            onClick = { onFlowSelected(WizardSetupFlow.SENIOR) }
        )
    }
}

@Composable
fun ContactPickerDialog(onDismiss: () -> Unit, onContactSelected: (String, String) -> Unit) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf(emptyList<Pair<String, String>>()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val list = mutableListOf<Pair<String, String>>()
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (it.moveToNext()) {
                    val name = it.getString(nameIdx)
                    val num = it.getString(numIdx)
                    if (name != null && num != null) list.add(name to num)
                }
            }
            contacts = list.distinctBy { it.second.replace(" ", "") }
            isLoading = false
        }
    }

    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { it.first.contains(searchQuery, ignoreCase = true) || it.second.contains(searchQuery) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().fillMaxHeight(0.9f),
            shape = RoundedCornerShape(40.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("KIES EEN CONTACT", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                
                Spacer(Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek op naam of nummer...", fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF94A3B8)) },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White.copy(alpha = 0.3f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                    )
                )

                Spacer(Modifier.height(20.dp))

                if (isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                } else {
                    LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredContacts) { contact ->
                            val interactionSource = remember { MutableInteractionSource() }
                            val pressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (pressed) 0.98f else 1f,
                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                            )

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable(interactionSource = interactionSource, indication = null) { 
                                        onContactSelected(contact.first, contact.second) 
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(56.dp).background(Color(0xFF3B82F6).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.first.take(1).uppercase(), fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF3B82F6))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(contact.first, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    Text(contact.second, fontSize = 16.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                val backInteractionSource = remember { MutableInteractionSource() }
                val backPressed by backInteractionSource.collectIsPressedAsState()
                val backScale by animateFloatAsState(
                    targetValue = if (backPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
                )

                OutlinedButton(
                    onClick = onDismiss, 
                    interactionSource = backInteractionSource,
                    modifier = Modifier.fillMaxWidth().height(80.dp).scale(backScale),
                    shape = RoundedCornerShape(40.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("ANNULEREN", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            }
        }
    }
}
