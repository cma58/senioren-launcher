package com.seniorenlauncher.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun TimeStepper(
    value: Int,
    range: IntRange,
    label: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp,
    buttonSize: Dp = 64.dp,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    val context = LocalContext.current
    val view = LocalView.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        IconButton(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                vibrateFeedback(context)
                val next = if (value >= range.last) range.first else value + 1
                onValueChange(next)
            },
            modifier = Modifier.size(buttonSize).background(containerColor, CircleShape)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(buttonSize * 0.55f))
        }

        Text(
            text = String.format(Locale.getDefault(), "%02d", value),
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        IconButton(
            onClick = {
                view.playSoundEffect(SoundEffectConstants.CLICK)
                vibrateFeedback(context)
                val prev = if (value <= range.first) range.last else value - 1
                onValueChange(prev)
            },
            modifier = Modifier.size(buttonSize).background(containerColor, CircleShape)
        ) {
            Icon(Icons.Default.Remove, null, modifier = Modifier.size(buttonSize * 0.55f))
        }
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

fun vibrateFeedback(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(30)
    }
}
