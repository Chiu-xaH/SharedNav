package com.xah.floating.component

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
import com.xah.container.util.LocalSharedRegistrySafely
import com.xah.floating.util.LocalFloatingControllerSafely
import kotlin.coroutines.cancellation.CancellationException

@Composable
actual fun FloatingBackHandler() {
    val controller = LocalFloatingControllerSafely.current ?: return
    val registry = LocalSharedRegistrySafely.current
    val canPop = controller.isRunning
    val context = LocalPlatformContext()

    if(registry?.enablePredictiveBack == true && EnableHelper.canPredictedGesture) {
        PredictiveBackHandler(enabled = canPop) { backEvents ->
            var state : SharedContainerState? = null
            try {
                val transiting = registry.isRunning
                if(!transiting) {
                    state = controller.startPredictiveBackShared()
                    LogUtil.debug("startPredictiveBack")
                }
                backEvents.collect { backEvent ->
                    if(!transiting) {
                        val progress = backEvent.progress
                        controller.updatePredictiveBackShared(progress, Offset.Zero,state)
                        LogUtil.debug("updatePredictiveBack $progress")
                    }
                }
                if(transiting) {
                    controller.pop()
                } else {
                    controller.confirmPredictiveBackShared(state)
                    LogUtil.debug("confirmPredictiveBack")
                }
            } catch (e: CancellationException) {
                controller.cancelPredictiveBackShared(state)
                LogUtil.debug("cancelPredictiveBack")
                throw e
            }
        }
    } else {
        BackHandler(enabled = canPop) {
            controller.pop()
        }
    }
}
