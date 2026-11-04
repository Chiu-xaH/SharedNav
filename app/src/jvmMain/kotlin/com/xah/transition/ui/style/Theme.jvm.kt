package com.xah.transition.ui.style

import androidx.compose.material3.ColorScheme
import com.sharednav.common.kmp.PlatformContext

actual val CAN_DYNAMIC_COLOR = false

actual fun getColorScheme(darkTheme: Boolean,context : PlatformContext): ColorScheme = when {
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
}