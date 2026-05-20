package com.seniorenlauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.seniorenlauncher.data.model.AppTheme

val ClassicColors = darkColorScheme(
    primary = Color(0xFF3B82F6), onPrimary = Color.White, secondary = Color(0xFF38A169),
    background = Color(0xFF0F1729), surface = Color(0xFF1A2440), surfaceVariant = Color(0xFF243352),
    onBackground = Color.White, onSurface = Color.White, onSurfaceVariant = Color(0xFF8B9DC3),
    error = Color(0xFFE53E3E)
)
val HighContrastColors = darkColorScheme(
    primary = Color(0xFF00CCFF), onPrimary = Color.Black, secondary = Color(0xFF00FF00),
    background = Color.Black, surface = Color(0xFF111111), surfaceVariant = Color(0xFF222222),
    onBackground = Color(0xFFFFFF00), onSurface = Color(0xFFFFFF00), onSurfaceVariant = Color.White,
    error = Color.Red
)
val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB), onPrimary = Color.White, secondary = Color(0xFF16A34A),
    background = Color(0xFFF0F2F5), surface = Color.White, surfaceVariant = Color(0xFFE4E7EC),
    onBackground = Color(0xFF1A202C), onSurface = Color(0xFF1A202C), onSurfaceVariant = Color(0xFF64748B),
    error = Color(0xFFDC2626)
)

@Composable
fun SeniorenLauncherTheme(
    appTheme: AppTheme = AppTheme.CLASSIC, 
    fontSize: Int = 18,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme() || appTheme != AppTheme.LIGHT
    
    val colorScheme = when {
        appTheme == AppTheme.HIGH_CONTRAST -> HighContrastColors
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        appTheme == AppTheme.LIGHT -> LightColors
        else -> ClassicColors
    }

    val scaleFactor = fontSize / 18f
    
    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density * scaleFactor,
        fontScale = currentDensity.fontScale * scaleFactor
    )

    CompositionLocalProvider(LocalDensity provides customDensity) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}
