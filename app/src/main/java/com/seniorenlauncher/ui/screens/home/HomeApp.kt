package com.seniorenlauncher.ui.screens.home

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HomeApp(
    val id: String,
    val name: String,
    val emoji: String? = null,
    val icon: Drawable? = null,
    val vectorIcon: ImageVector? = null,
    val color: Color,
    val weatherOverlay: String? = null
)
