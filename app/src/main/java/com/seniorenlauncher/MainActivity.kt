package com.seniorenlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seniorenlauncher.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // De verkeersregelaar: houdt bij welk scherm we momenteel zien
                    var currentScreen by remember { mutableStateOf("home") }
                    
                    // Zorgt ervoor dat de fysieke 'Terug'-knop van je telefoon werkt
                    if (currentScreen != "home") {
                        BackHandler { currentScreen = "home" }
                    }

                    // Laadt de instellingen van de app in
                    val settingsViewModel: SettingsViewModel = viewModel()

                    // Hier verbinden we de knoppen met de juiste schermen!
                    when (currentScreen) {
                        "home" -> HomeScreen(onNav = { currentScreen = it }, vm = settingsViewModel)
                        "phone" -> ContactsScreen(onCall = {}, onBack = { currentScreen = "home" })
                        "sms" -> MessagesScreen(onBack = { currentScreen = "home" })
                        "flashlight" -> FlashlightScreen(onBack = { currentScreen = "home" })
                        "emergency" -> EmergencyInfoScreen(onBack = { currentScreen = "home" })
                        "alarm" -> AlarmScreen(onBack = { currentScreen = "home" })
                        "calendar" -> CalendarScreen(onBack = { currentScreen = "home" })
                        "sos" -> EmergencyInfoScreen(onBack = { currentScreen = "home" })
                        // Als de app een knop nog niet kent, blijf dan veilig op het startscherm
                        else -> HomeScreen(onNav = { currentScreen = it }, vm = settingsViewModel)
                    }
                }
            }
        }
    }
}
