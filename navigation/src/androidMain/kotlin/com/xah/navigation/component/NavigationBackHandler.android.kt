package com.xah.navigation.component

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.geometry.Offset
import com.sharednav.common.helper.EnableHelper
import com.sharednav.common.kmp.LocalPlatformContext
import com.sharednav.common.util.LogUtil
import com.xah.container.model.SharedContainerState
import com.xah.navigation.util.LocalNavControllerSafely
import kotlin.coroutines.cancellation.CancellationException

@Composable
actual fun NavigationBackHandler() {
    val navController = LocalNavControllerSafely.current ?: return
    val canPop = navController.canPop()
    val context = LocalPlatformContext()

    if(navController.enablePredictiveBack && EnableHelper.canPredictedGesture) {
        PredictiveBackHandler(enabled = canPop) { backEvents ->
            var state : SharedContainerState? = null
            try {
                val transiting = navController.isTransitioning
                if(!transiting) {
                    state = navController.startPredictiveBackShared()
                    LogUtil.debug("startPredictiveBack")
                }
                backEvents.collect { backEvent ->
                    if(!transiting) {
                        val progress = backEvent.progress
                        navController.updatePredictiveBackShared(progress, Offset.Zero,state)
                        LogUtil.debug("updatePredictiveBack $progress")
                    }
                }
                if(transiting) {
                    navController.pop()
                } else {
                    navController.confirmPredictiveBackShared(state)
                    LogUtil.debug("confirmPredictiveBack")
                }
            } catch (e: CancellationException) {
                navController.cancelPredictiveBackShared(state)
                LogUtil.debug("cancelPredictiveBack")
                throw e
            }
        }
    } else {
        BackHandler(enabled = canPop) {
            navController.pop()
        }
    }
}
