package com.xah.navigation.component

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.sharednav.common.util.LogUtil
import com.xah.navigation.util.LocalNavController

private var initialRequestedOrientation : Int? = null

@Composable
internal actual fun ScreenOrientationEffect() {
    val activity = LocalActivity.current
    val navController = LocalNavController.current
    val action = navController.transitionEntry?.type

    LaunchedEffect (activity,action) {
        if(activity == null) {
            return@LaunchedEffect
        }
        if(initialRequestedOrientation == null) {
            initialRequestedOrientation = activity.requestedOrientation
        }
        navController.awaitTransition()
        val destination = navController.currentDestination()
        (destination.requestedOrientation ?: initialRequestedOrientation)?.let {
            LogUtil.debug("设置requestedOrientation=${it}")
            activity.requestedOrientation = it
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}