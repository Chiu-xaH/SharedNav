package com.sharednav.common.kmp

import androidx.compose.runtime.Composable

@Composable
actual fun LocalPlatformActivity() = PlatformActivity()

@Composable
actual fun LocalPlatformContext() = PlatformContext()

@Composable
actual fun LocalPlatformView() = PlatformView()