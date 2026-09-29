package com.sharednav.common.kmp

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@Composable
actual fun LocalPlatformActivity(): PlatformActivity {
    val activity = LocalActivity.current
    return PlatformActivity(activity)
}

@Composable
actual fun LocalPlatformContext(): PlatformContext {
    val context = LocalContext.current
    return PlatformContext(context)
}

@Composable
actual fun LocalPlatformView(): PlatformView {
    val view = LocalView.current
    return PlatformView(view)
}