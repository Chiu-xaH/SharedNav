package com.xah.transition.ui.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sharednav.common.kmp.PlatformContext
import com.sharednav.common.kmp.LocalPlatformContext

expect val CAN_DYNAMIC_COLOR : Boolean

val DarkColorScheme = darkColorScheme()

val LightColorScheme = lightColorScheme()

expect fun getColorScheme(darkTheme : Boolean,context : PlatformContext) : ColorScheme

@Composable
fun TransitionTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalPlatformContext()
    val colorScheme = getColorScheme(darkTheme,context)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

val Typography = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)