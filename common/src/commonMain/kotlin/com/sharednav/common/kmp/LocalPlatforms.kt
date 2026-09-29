package com.sharednav.common.kmp

import androidx.compose.runtime.Composable

@Composable
expect fun LocalPlatformActivity() : PlatformActivity

@Composable
expect fun LocalPlatformContext() : PlatformContext

@Composable
expect fun LocalPlatformView() : PlatformView