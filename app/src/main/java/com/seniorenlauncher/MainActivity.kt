package com.seniorenlauncher

import android.app.KeyguardManager
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
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.ui.AppNavigation
import com.seniorenlauncher.ui.components.RadioMiniPlayer
import com.seniorenlauncher.ui.screens.*
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme
import com.seniorenlauncher.service.SeniorInCallService
import com.seniorenlauncher.util.UpdateManager
import android.telecom.Call
import android.util.Log

class MainActivity : ComponentActivity() {
    
    private var currentAlarmId = mutableStateOf<Long>(-1L)
    private var alarmTriggered = mutableStateOf<String?>(null)
    private var alarmSoundUri = mutableStateOf<String?>(null)
    private var alarmIsRemote = mutableStateOf(false)
    private var navigateToSmsAddress = mutableStateOf<String?>(null)
    private var navigateToIncomingCall = mutableStateOf(false)
    private var navigateToWeatherAfterAlarm = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent?.getStringExtra("NAVIGATE_TO") == "alarm_trigger") {
            setupLockscreenBypass()
        }
        
        super.onCreate(savedInstanceState)
        
        // Android 15/16 Edge-to-Edge ondersteuning
        enableEdgeToEdge()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        handleIntent(intent)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Predictive Back support: do not intercept if there's no custom UI showing
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
            val isRemote by alarmIsRemote
            val smsAddress by navigateToSmsAddress
            val showIncomingCall by navigateToIncomingCall
            val shouldNavToWeather by navigateToWeatherAfterAlarm
            
            val currentCall by SeniorInCallService.currentCall.collectAsState()
            
            var currentRoute by remember { mutableStateOf<String?>(null) }

            // Auto-update check bij opstarten
            LaunchedEffect(Unit) {
                val updateManager = UpdateManager(this@MainActivity)
                val release = updateManager.checkForUpdates()
                if (release != null) {
                    updateManager.downloadAndInstall(release)
                }
            }

            if (triggerLabel != null) {
                LaunchedEffect(Unit) {
                    setupLockscreenBypass()
                }
            }

            SeniorenLauncherTheme(appTheme = settings.theme, fontSize = settings.fontSize) {
                Surface(Modifier.fillMaxSize()) {
                    if (triggerLabel != null) {
                        AlarmTriggerScreen(
                            label = triggerLabel!!,
                            alarmId = alarmId,
                            soundName = soundUri,
                            isForcedRemote = isRemote,
                            onDismiss = {
                                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                nm.cancel(alarmId.toInt())
                                if (triggerLabel!!.startsWith("Medicijn:")) {
                                    nm.cancel(alarmId.toInt() + 5000)
                                }
                                alarmTriggered.value = null
                                alarmSoundUri.value = null
                                alarmIsRemote.value = false
                                currentAlarmId.value = -1L
                            },
                            onNavigateToWeather = {
                                navigateToWeatherAfterAlarm.value = true
                            }
                        )
                    } else if (!settings.hasCompletedSetup) {
                        SetupWizardScreen(
                            onFinished = { settingsVm.completeSetup() },
                            settingsVm = settingsVm
                        )
                    } else {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            Box(Modifier.weight(1f)) {
                                AppNavigation(
                                    settingsVm = settingsVm, 
                                    radioVm = radioVm, 
                                    initialSmsAddress = smsAddress,
                                    initialIncomingCall = showIncomingCall || (currentCall != null && currentCall?.state == Call.STATE_RINGING),
                                    initialWeatherNav = shouldNavToWeather,
                                    onNavigatedToSms = { navigateToSmsAddress.value = null },
                                    onNavigatedToCall = { navigateToIncomingCall.value = false },
                                    onNavigatedToWeather = { navigateToWeatherAfterAlarm.value = false },
                                    onRouteChanged = { currentRoute = it }
                                )
                            }
                            if (currentRoute != "radio") {
                                RadioMiniPlayer(
                                    radioVm = radioVm,
                                    onOpenRadio = {
                                        // Optioneel: Navigeer naar radio
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupLockscreenBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getStringExtra("NAVIGATE_TO") == "alarm_trigger") {
            setupLockscreenBypass()
        }
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val navigateTo = it.getStringExtra("NAVIGATE_TO")
            if (navigateTo == "alarm_trigger") {
                val label = it.getStringExtra("ALARM_LABEL") ?: "Wekker"
                val id = it.getLongExtra("ALARM_ID", -1L)
                val isRemote = it.getStringExtra("agendaTitle") == "BERICHT VAN BEHEERDER"
                
                Log.d("MainActivity", "Wekker ontvangen: $label met ID: $id (Remote: $isRemote)")
                alarmTriggered.value = label
                currentAlarmId.value = id
                alarmIsRemote.value = isRemote
                alarmSoundUri.value = it.getStringExtra("ALARM_SOUND")
            } else if (navigateTo == "sms") {
                navigateToSmsAddress.value = it.getStringExtra("SMS_ADDRESS")
            } else if (navigateTo == "incoming_call") {
                navigateToIncomingCall.value = true
            }
        }
    }
}
