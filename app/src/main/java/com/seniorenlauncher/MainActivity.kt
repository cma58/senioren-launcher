package com.seniorenlauncher

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seniorenlauncher.ui.screens.*
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

class MainActivity : ComponentActivity() {
    
    private var currentAlarmId = mutableStateOf<Long?>(-1L)
    private var alarmTriggered = mutableStateOf<String?>(null)
    private var alarmSoundUri = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        handleIntent(intent)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (alarmTriggered.value == null) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })

        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val radioVm: RadioViewModel = viewModel()
            val settings by settingsVm.settings.collectAsState()
            
            val triggerLabel by alarmTriggered
            val alarmId by currentAlarmId
            val soundUri by alarmSoundUri

            SeniorenLauncherTheme(appTheme = settings.theme, fontSize = settings.fontSize) {
                Surface(Modifier.fillMaxSize()) {
                    if (triggerLabel != null) {
                        AlarmTriggerScreen(
                            label = triggerLabel!!,
                            soundName = soundUri,
                            onDismiss = {
                                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                nm.cancel(alarmId?.toInt() ?: -1)
                                alarmTriggered.value = null
                                alarmSoundUri.value = null
                            }
                        )
                    } else if (!settings.hasCompletedSetup) {
                        SetupWizardScreen(
                            onFinished = { settingsVm.completeSetup() },
                            settingsVm = settingsVm
                        )
                    } else {
                        AppNavigation(settingsVm, radioVm)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val navigateTo = it.getStringExtra("NAVIGATE_TO")
            if (navigateTo == "alarm_trigger") {
                alarmTriggered.value = it.getStringExtra("ALARM_LABEL") ?: "Wekker"
                currentAlarmId.value = it.getLongExtra("ALARM_ID", -1L)
                alarmSoundUri.value = it.getStringExtra("ALARM_SOUND")
            }
        }
    }
}

@Composable
fun AppNavigation(settingsVm: SettingsViewModel, radioVm: RadioViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { 
            HomeScreen(
                onNavigate = { navController.navigate(it) },
                settingsVm = settingsVm,
                radioVm = radioVm
            )
        }
        composable("settings") { 
            SettingsScreen(
                vm = settingsVm,
                onBack = { navController.popBackStack() },
                onNavigate = { navController.navigate(it) }
            ) 
        }
        composable("radio") { 
            RadioScreen(
                onBack = { navController.popBackStack() },
                radioVm = radioVm
            ) 
        }
        composable("alarm") { 
            AlarmScreen(
                onBack = { navController.popBackStack() }
            ) 
        }
        composable("contacts") { 
            ContactsScreen(
                onBack = { navController.popBackStack() }
            ) 
        }
        composable("sms") { 
            MessagesScreen(
                onBack = { navController.popBackStack() },
                settingsVm = settingsVm
            ) 
        }
        composable("phone") {
            PhoneScreen(
                onNavigate = { navController.navigate(it) },
                onBack = { navController.popBackStack() },
                settingsVm = settingsVm
            )
        }
        composable("emergency") {
            EmergencyInfoScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("meds") {
            MedicationScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("sos") {
            SOSScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("sos_settings") {
            SosContactSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("all_apps") {
            AllAppsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("photos") {
            PhotosScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("weather") {
            WeatherScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("remote_support") {
            RemoteSupportScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("calendar") {
            CalendarScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("flashlight") {
            FlashlightScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("magnifier") {
            MagnifierScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("notes") {
            NotesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("steps") {
            StepsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("apps") { Box(Modifier.fillMaxSize()) { Text("Apps Screen", Modifier.align(Alignment.Center)) } }
        composable("gallery") { Box(Modifier.fillMaxSize()) { Text("Gallery Screen", Modifier.align(Alignment.Center)) } }
    }
}
