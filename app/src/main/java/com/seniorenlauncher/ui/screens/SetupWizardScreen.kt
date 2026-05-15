package com.seniorenlauncher.ui.screens

import android.content.Context
import android.os.Build
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.PrivacyTip,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            text = "Privacy & Veiligheid",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                PrivacyPoint(
                    icon = Icons.Default.Lock,
                    title = "Uw data blijft van u",
                    description = "Alle contacten, SOS-nummers en instellingen worden alleen veilig op dít toestel opgeslagen. Wij verzamelen geen persoonlijke gegevens."
                )
                
                Spacer(Modifier.height(16.dp))
                
                PrivacyPoint(
                    icon = Icons.Default.CloudOff,
                    title = "Geen Cloud-opslag",
                    description = "Er wordt geen data naar externe servers of de cloud gestuurd. Uw privacy is 100% gewaarborgd volgens de Europese AVG/GDPR normen."
                )
                
                Spacer(Modifier.height(16.dp))
                
                PrivacyPoint(
                    icon = Icons.Default.AdsClick,
                    title = "Geen Advertenties",
                    description = "De Senioren Launcher is volledig vrij van reclame en tracking software."
                )
            }
        }
        
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = "Door op de knop hieronder te drukken, gaat u akkoord met onze privacyverklaring.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = onAccepted,
            modifier = Modifier.fillMaxWidth().height(70.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("IK GA AKKOORD", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FlowSelectionScreen(onFlowSelected: (WizardSetupFlow) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welkom.\nWie stelt deze telefoon in?",
            fontSize = 32.sp,
            lineHeight = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "De setup bestaat uit twee delen: technische instellingen door de beheerder en visuele keuzes voor de senior.",
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        SetupOptionCard(
            title = "Ik ben de Mantelzorger",
            description = "Ik stel dit toestel technisch in (aanbevolen).",
            icon = Icons.Default.VolunteerActivism,
            color = Color(0xFF3B82F6),
            onClick = { onFlowSelected(WizardSetupFlow.CAREGIVER) }
        )

        Spacer(Modifier.height(24.dp))

        SetupOptionCard(
            title = "Ik ben de Gebruiker",
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
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Kies een contact", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Zoek op naam of nummer...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(Modifier.weight(1f)) {
                        items(filteredContacts) { contact ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { onContactSelected(contact.first, contact.second) }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(44.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(contact.first.take(1).uppercase(), fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(contact.first, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(contact.second, fontSize = 14.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
                
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("ANNULEREN")
                }
            }
        }
    }
}
