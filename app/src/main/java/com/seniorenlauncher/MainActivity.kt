package com.seniorenlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.seniorenlauncher.ui.screens.*
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            val theme by vm.currentTheme.collectAsState()
            SeniorenLauncherTheme(appTheme = theme) {
                Surface(Modifier.fillMaxSize()) { AppNavigation(vm) }
            }
        }
    }
    @Deprecated("Deprecated") override fun onBackPressed() {}
}

@Composable
fun AppNavigation(vm: SettingsViewModel) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "home") {
        composable("home") { HomeScreen(onNavigate = { nav.navigate(it) }, settingsVm = vm) }
        composable("phone") { PhoneScreen(onNavigate = { nav.navigate(it) }, onBack = { nav.popBackStack() }) }
        composable("contacts") { ContactsScreen(onCall = { nav.navigate("calling") }, onBack = { nav.popBackStack() }) }
        composable("calling") { CallingScreen(onEnd = { nav.popBackStack("home", false) }) }
        composable("sms") { MessagesScreen(onBack = { nav.popBackStack() }) }
        composable("calendar") { CalendarScreen(onBack = { nav.popBackStack() }) }
        composable("meds") { MedicationScreen(onBack = { nav.popBackStack() }) }
        composable("weather") { WeatherScreen(onBack = { nav.popBackStack() }) }
        composable("alarm") { AlarmScreen(onBack = { nav.popBackStack() }) }
        composable("flashlight") { FlashlightScreen(onBack = { nav.popBackStack() }) }
        composable("magnifier") { MagnifierScreen(onBack = { nav.popBackStack() }) }
        composable("photos") { PhotosScreen(onBack = { nav.popBackStack() }) }
        composable("notes") { NotesScreen(onBack = { nav.popBackStack() }) }
        composable("emergency") { EmergencyInfoScreen(onBack = { nav.popBackStack() }) }
        composable("steps") { StepsScreen(onBack = { nav.popBackStack() }) }
        composable("radio") { RadioScreen(onBack = { nav.popBackStack() }) }
        composable("sos") { SOSScreen(onBack = { nav.popBackStack() }) }
        composable("notifications") { NotificationsScreen(onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm = vm, onBack = { nav.popBackStack() }) }
    }
}
