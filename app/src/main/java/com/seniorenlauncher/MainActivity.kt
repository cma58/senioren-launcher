package com.seniorenlauncher

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.seniorenlauncher.ui.screens.*
import com.seniorenlauncher.ui.theme.SeniorenLauncherTheme

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val radioVm: RadioViewModel = viewModel()
            val settings by settingsVm.settings.collectAsState()
            
            var showPermissionCheck by remember { mutableStateOf(true) }

            SeniorenLauncherTheme(appTheme = settings.theme) {
                Surface(Modifier.fillMaxSize()) {
                    if (showPermissionCheck) {
                        PermissionScreen(onFinished = { showPermissionCheck = false })
                    } else {
                        AppNavigation(settingsVm, radioVm)
                    }
                }
            }
        }
    }
    @Deprecated("Deprecated") override fun onBackPressed() {
        super.onBackPressed()
    }
}

@Composable
fun PermissionScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var permissionsState by remember { mutableStateOf(mapOf<String, Boolean>()) }

    fun checkPermissions() {
        val state = mutableMapOf<String, Boolean>()
        
        state["post_notifications"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        
        state["phone"] = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        state["contacts"] = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        
        val hasReadSms = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val hasSendSms = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val hasReceiveSms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        state["sms"] = hasReadSms && hasSendSms && hasReceiveSms

        state["location"] = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        state["photos"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        state["notification_access"] = enabledListeners?.contains(context.packageName) == true
        
        state["exact_alarm"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
            alarmManager.canScheduleExactAlarms()
        } else true

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        state["battery"] = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        // Check if we are the default launcher
        state["launcher"] = isDefaultLauncher(context)

        permissionsState = state
    }

    LaunchedEffect(Unit) { checkPermissions() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        checkPermissions()
    }

    val roleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkPermissions()
    }

    val allGranted = permissionsState.values.all { it }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Installatie hulp", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Laten we de telefoon samen goed zetten voor een makkelijk en veilig gebruik.",
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(32.dp))

        PermissionItem("1. Standaard App maken", permissionsState["launcher"] == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(RoleManager::class.java)
                if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                    roleLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
                }
            } else {
                context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            }
        }

        PermissionItem("2. Batterij besparing uit", permissionsState["battery"] == true) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }

        PermissionItem("3. Meldingen weergeven", permissionsState["post_notifications"] == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
        }
        
        PermissionItem("4. Bellen & Contacten", (permissionsState["phone"] == true && permissionsState["contacts"] == true)) {
            launcher.launch(arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS))
        }

        PermissionItem("5. Berichten (SMS)", permissionsState["sms"] == true) {
            launcher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS))
        }

        PermissionItem("6. Foto's bekijken", permissionsState["photos"] == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            } else {
                launcher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }

        PermissionItem("7. Locatie (voor SOS)", permissionsState["location"] == true) {
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        PermissionItem("8. Meldingen lezen (Badges)", permissionsState["notification_access"] == true) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        PermissionItem("9. Exacte Wekker (Medicijnen)", permissionsState["exact_alarm"] == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }

        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = { if (allGranted) onFinished() else checkPermissions() },
            modifier = Modifier.fillMaxWidth().height(70.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allGranted) MaterialTheme.colorScheme.primary else Color.Gray
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (allGranted) "Klaar! Start de App" else "Zet eerst alles op ✅", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun isDefaultLauncher(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val res = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return res?.activityInfo?.packageName == context.packageName
}

@Composable
fun PermissionItem(label: String, granted: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { if (!granted) onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (granted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = if (granted) FontWeight.Normal else FontWeight.Bold,
                color = if (granted) Color(0xFF2E7D32) else Color.Unspecified
            )
            if (granted) {
                Text("✅", fontSize = 24.sp)
            } else {
                Text("👉 Tik hier", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AppNavigation(settingsVm: SettingsViewModel, radioVm: RadioViewModel) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "home") {
        composable("home") { HomeScreen(onNavigate = { nav.navigate(it) }, settingsVm = settingsVm, radioVm = radioVm) }
        composable("phone") { PhoneScreen(onNavigate = { nav.navigate(it) }, onBack = { nav.popBackStack() }, settingsVm = settingsVm) }
        composable("contacts") { ContactsScreen(onCall = { nav.navigate("calling") }, onBack = { nav.popBackStack() }) }
        composable("calling") { CallingScreen(onEnd = { nav.popBackStack("home", false) }) }
        composable(
            route = "sms?address={address}&name={name}",
            arguments = listOf(
                navArgument("address") { type = NavType.StringType; nullable = true },
                navArgument("name") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            MessagesScreen(
                onBack = { nav.popBackStack() },
                settingsVm = settingsVm,
                initialAddress = backStackEntry.arguments?.getString("address"),
                initialName = backStackEntry.arguments?.getString("name")
            )
        }
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
        composable("radio") { RadioScreen(onBack = { nav.popBackStack() }, radioVm = radioVm) }
        composable("sos") { SOSScreen(onBack = { nav.popBackStack() }) }
        composable("sos_settings") { SosContactSettingsScreen(onBack = { nav.popBackStack() }) }
        composable("notifications") { NotificationsScreen(onBack = { nav.popBackStack() }) }
        composable("settings") { SettingsScreen(vm = settingsVm, onNavigate = { nav.navigate(it) }, onBack = { nav.popBackStack() }) }
        composable("all_apps") { AllAppsScreen(onBack = { nav.popBackStack() }) }
    }
}
