package com.seniorenlauncher.ui

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.seniorenlauncher.ui.screens.*

@Composable
fun AppNavigation(
    settingsVm: SettingsViewModel,
    radioVm: RadioViewModel,
    initialSmsAddress: String? = null,
    initialIncomingCall: Boolean = false,
    initialWeatherNav: Boolean = false,
    onNavigatedToSms: () -> Unit = {},
    onNavigatedToCall: () -> Unit = {},
    onNavigatedToWeather: () -> Unit = {}
) {
    val navController = rememberNavController()

    LaunchedEffect(initialSmsAddress) {
        initialSmsAddress?.let {
            navController.navigate("sms_detail/$it")
            onNavigatedToSms()
        }
    }

    LaunchedEffect(initialIncomingCall) {
        if (initialIncomingCall) {
            navController.navigate("incoming_call")
            onNavigatedToCall()
        }
    }

    LaunchedEffect(initialWeatherNav) {
        if (initialWeatherNav) {
            navController.navigate("weather")
            onNavigatedToWeather()
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) },
                settingsVm = settingsVm,
                radioVm = radioVm
            )
        }
        composable("phone") { 
            PhoneScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            ) 
        }
        composable("sms") { MessagesScreen(onBack = { navController.popBackStack() }) }
        composable(
            "sms_detail/{address}",
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address")
            MessagesScreen(onBack = { navController.popBackStack() }, initialAddress = address)
        }
        composable("camera") { /* Handled in HomeScreen via AppLauncher */ }
        composable("photos") { PhotosScreen(onBack = { navController.popBackStack() }) }
        composable("alarm") { AlarmScreen(onBack = { navController.popBackStack() }) }
        composable("calendar") { CalendarScreen(onBack = { navController.popBackStack() }) }
        composable("meds") { MedicationScreen(onBack = { navController.popBackStack() }) }
        composable("weather") { WeatherScreen(onBack = { navController.popBackStack() }) }
        composable("flashlight") { FlashlightScreen(onBack = { navController.popBackStack() }) }
        composable("magnifier") { MagnifierScreen(onBack = { navController.popBackStack() }) }
        composable("notes") { NotesScreen(onBack = { navController.popBackStack() }) }
        composable("radio") { RadioScreen(onBack = { navController.popBackStack() }) }
        composable("steps") { StepsScreen(onBack = { navController.popBackStack() }) }
        composable("emergency") { EmergencyInfoScreen(onBack = { navController.popBackStack() }) }
        composable("sos") { SOSScreen(onBack = { navController.popBackStack() }) }
        composable("sos_settings") { SosContactSettingsScreen(onBack = { navController.popBackStack() }) }
        composable("remote_support") { RemoteSupportScreen(onBack = { navController.popBackStack() }) }
        composable("all_apps") { AllAppsScreen(onBack = { navController.popBackStack() }) }
        composable("settings") { 
            SettingsScreen(
                vm = settingsVm, 
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            ) 
        }
        composable("notifications") { 
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            ) 
        }
        composable("incoming_call") { IncomingCallScreen() }
    }
}
