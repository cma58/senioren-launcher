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
    primary = Color(0xFF6366F1), // Modern Indigo
    onPrimary = Color.White,
    secondary = Color(0xFF10B981), // Emerald
    tertiary = Color(0xFFF59E0B), // Amber
    background = Color(0xFF0F172A), // Slate 900
    surface = Color(0xFF1E293B), // Slate 800
    surfaceVariant = Color(0xFF334155), // Slate 700
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444)
)
val HighContrastColors = darkColorScheme(
    primary = Color(0xFF00CCFF), onPrimary = Color.Black, secondary = Color(0xFF00FF00),
    background = Color.Black, surface = Color(0xFF111111), surfaceVariant = Color(0xFF222222),
    onBackground = Color(0xFFFFFF00), onSurface = Color(0xFFFFFF00), onSurfaceVariant = Color.White,
    error = Color.Red
)
val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    secondary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF64748B),
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
