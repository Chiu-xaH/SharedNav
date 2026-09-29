package com.xah.transition.style

import androidx.compose.material3.ColorScheme
import com.sharednav.common.kmp.PlatformContext
import com.xah.transition.ui.style.DarkColorScheme
import com.xah.transition.ui.style.LightColorScheme

actual val CAN_DYNAMIC_COLOR = false

actual fun getColorScheme(darkTheme: Boolean,context : PlatformContext): ColorScheme = when {
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}