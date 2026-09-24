package com.xah.container.controller

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.sharednav.common.helper.EnableHelper
import com.sharednav.common.manager.AnimationSpecManager
import com.sharednav.common.util.LogUtil
import com.sharednav.common.util.PredictiveUtil
import com.xah.container.anim.LinearRectInterpolator
import com.xah.container.anim.QuadraticBezierRectInterpolator
import com.xah.container.anim.RectInterpolator
import com.xah.container.model.ContainerFilledStrategy
import com.xah.container.model.ContentStrategy
import com.xah.container.model.SharedContainerState
import com.xah.container.model.SpeedUpRadio
import com.xah.container.model.StatePause
import com.xah.container.model.TiltEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class SharedRegistry(
    private val scope: CoroutineScope,
    private val states : SnapshotStateMap<String, SharedContainerState>
) {
    val runningStates: List<SharedContainerState> by derivedStateOf {
        states.values.filter { it.isRunning() }
    }
    val isRunning: Boolean by derivedStateOf {
        states.values.any { it.isRunning() }
    }
    val isWaitingFrame: Boolean by derivedStateOf {
        states.values.any { it.currentState == StatePause.MEASURING_CONTAINER || it.currentState == StatePause.MEASURING_CONTENT }
    }

    var enablePredictiveBack by mutableStateOf(EnableHelper.canShader)

    private fun SharedContainerState.isRunning() = currentState.isTransiting() && containerRect != null && contentRect != null

    var enabled by mutableStateOf(true)

    /**
     * 强制使用某种填充方式,null为不强制
     */
    var enforceContainerFilledStrategy by mutableStateOf<ContainerFilledStrategy?>(null)

    /**
     * 最大等帧时长，为什么需要等，本质上取决于导航栈的设计，如果导航栈只能保持一个页面，其余页面都被销毁，当POP时需要等下面的初始化完成，才能记录容器位置，如果栈中内容都不销毁，那么就只需要等1帧（16ms）
     * 如果waitFrameMaxValue=0或者enableWaitFrameMaxValue=false，则等1帧
     * 界面越复杂，性能越差，需要等的帧越大，但是一般在10帧以内
     */
    var waitFrameMaxValue by mutableIntStateOf(10)

    // 自定义预设曲线
    var pushX1 by mutableFloatStateOf(0.4f)
    var pushY1 by mutableFloatStateOf(0.65f)
    var pushX2 by mutableFloatStateOf(0.25f)
    var pushY2 by mutableFloatStateOf(1.0f)

    var popX1 by mutableFloatStateOf(0.4f)
    var popY1 by mutableFloatStateOf(0.65f)
    var popX2 by mutableFloatStateOf(0.15f)
    var popY2 by mutableFloatStateOf(1.0f)


    fun <T> getPushAnimation() = tween<T>(AnimationSpecManager.getSharedTween(), easing = CubicBezierEasing(pushX1,pushY1,pushX2,pushY2))
    fun <T> getPopAnimation() = tween<T>(AnimationSpecManager.getSharedTween(), easing = CubicBezierEasing(popX1,popY1,popX2,popY2))

    var screenRect : Rect? = null

    // 渐隐、圆角、倾斜变化比容器变化时长
    var speedUpRadioAlpha by mutableFloatStateOf(SpeedUpRadio.default.alpha)
    var speedUpRadioCorner by mutableFloatStateOf(SpeedUpRadio.default.corner)
    var speedUpRadioTilt by mutableFloatStateOf(SpeedUpRadio.default.tilt)

    // 形变时插入一个中间态（圆形）
    var enforceQuadraticCornerLerp by mutableStateOf(false)
    var quadraticCornerLerpFactor by mutableFloatStateOf(0.5f)

    // 倾斜效果
    var tiltEffect by mutableStateOf(TiltEffect.ROTATION)
    // 最大变化值
    var tiltMaxValue by mutableFloatStateOf(17.5f)

    // 单边填充or双边填充
    var extensionDouble by mutableStateOf(false)

    var enableShader by mutableStateOf(EnableHelper.canShader)

    var quadraticBezierRectInterpolatorVerticalRadio by mutableFloatStateOf(2f)
    var quadraticBezierRectInterpolatorHorizontalRadio by mutableFloatStateOf(3f)

    fun register(
        key: String,
    ): SharedContainerState = states.getOrPut(key) {
        LogUtil.debug("register key: $key")
        SharedContainerState(key)
    }

    fun unregister(state : SharedContainerState) = unregister(state.key)

    fun unregister(key: String) {
        LogUtil.debug("unregister key: $key")
        states.remove(key)
    }

    fun get(
        key: String,
        contentStrategy : ContentStrategy
    ): SharedContainerState? {
        val state = states[key]
        state?.contentStrategy = contentStrategy
        return state
    }

    fun push(
        key: String,
        onAnimatedFinished : (suspend () -> Unit)? = null,
        onSwap: () -> Unit
    ) {
        scope.launch {
            pushInternal(key,onAnimatedFinished, onSwap)
        }
    }

    fun pop(
        key: String,
        onAnimatedFinished : (suspend () -> Unit)? = null,
        onSwap: () -> Unit
    ) {
        scope.launch {
            popInternal(key, onAnimatedFinished,onSwap)
        }
    }

    private suspend fun pushInternal(
        key: String,
        onAnimatedFinished : (suspend () -> Unit)? = null,
        onSwap: () -> Unit
    ) {
        val state = states[key]
        if(state == null) {
            onSwap()
            return
        }
        pushInternal(state,onAnimatedFinished,onSwap)
    }

    private suspend fun popInternal(
        key: String,
        onAnimatedFinished : (suspend () -> Unit)? = null,
        onSwap: () -> Unit
    ) {
        val state = states[key]
        if(state == null) {
            onSwap()
            return
        }
        popInternal(state,onAnimatedFinished,onSwap)
    }

    private suspend fun pushInternal(
        state: SharedContainerState,
        onAnimatedFinished : (suspend () -> Unit)? = null,
        onSwap: () -> Unit
    ) {
        if(state.containerRect == null) {
            onSwap()
            state.currentState = StatePause.CONTENT
            return
        }
        // container destroy的时候不启用动画
        if(state.isInActive()) {
            onSwap()
            unregister(state)
            LogUtil.debug("push without shared ${state.key}")
            return
        }
        if(!waitContentFrame(state,onSwap)) {
            return
        }
        snap(state,true)
        // 开始标识位
        LogUtil.debug("currentState=${state.currentState}")
        state.currentState = state.currentState.getTargetTransiting()
        state.animation.animateTo(1f,getPushAnimation())
        onAnimatedFinished?.let { it() }
        state.containerRect = null
        // 结束标志位
        state.currentState = StatePause.CONTENT
    }

    private suspend fun popInternal(
        state: SharedContainerState,
        onAnimatedFinished : (suspend () -> Unit)? = null,
        onSwap: () -> Unit
    ) {
        if(!waitContainerFrame(state,onSwap)) {
            return
        }
        // 重置位置
        snap(state,false)
        // 开始标识位
        LogUtil.debug("currentState=${state.currentState}")
        state.currentState = state.currentState.getTargetTransiting()
        state.animation.animateTo(0f,getPopAnimation())
        onAnimatedFinished?.let { it() }
        state.contentRect = null
        // 结束标志位
        state.currentState = StatePause.CONTAINER
    }

    fun cancelPop() : SharedContainerState? {
        val state = runningStates.firstOrNull() ?: return null
        state.currentState = StatePause.CONTAINER
        return state
    }

    private suspend fun snap(
        state: SharedContainerState,
        isPush : Boolean
    ) {
        // 状态复位
        if(!state.currentState.isTransiting()) {
            // 状态复位
            state.animation.snapTo(
                if(isPush) {
                    0f
                } else {
                    1f
                }
            )
        }
    }

    suspend fun awaitTransition() = snapshotFlow { isRunning }.filter { !it }.first()

    private val offsetAnim = Animatable(Offset.Zero, Offset.VectorConverter)

    private fun resetOffset(state: SharedContainerState) {
        state.contentOffset = Offset.Zero
    }

    private suspend fun resetOffsetSmoothly(state: SharedContainerState) {
        offsetAnim.snapTo(state.contentOffset)

        offsetAnim.animateTo(
            targetValue = Offset.Zero,
            animationSpec = tween(200)
//            animationSpec = PredictiveUtil.cancelAnimation()
        ) {
            state.contentOffset = value
        }
    }

    private suspend fun waitContainerFrame(
        state: SharedContainerState,
        onSwap: () -> Unit,
    ): Boolean = waitFrame(state,true,onSwap)

    private suspend fun waitContentFrame(
        state: SharedContainerState,
        onSwap: () -> Unit,
    ): Boolean = waitFrame(state,false,onSwap)

    /**
     * 返回时，有些容器是动态加载的，等一个动画时长，如果超时了按导航默认动画走（超时请优化写法）
     */
    private suspend fun waitFrame(
        state: SharedContainerState,
        isContainer : Boolean,
        onSwap: () -> Unit,
    ): Boolean {
        require(waitFrameMaxValue >= 1) {
            error("waitFrameMaxValue must >= 1")
        }

        var frameCount = 0
        // 状态复位
        if(!state.currentState.isTransiting()) {
            state.currentState = if(isContainer) {
                StatePause.CONTENT
            } else {
                StatePause.CONTAINER
            }
        }
        // 一定要确保页面切换(onSwap)之后马上等帧(awaitFrame)
        onSwap()
        when(state.currentState) {
            StatePause.CONTENT -> {
                state.currentState = StatePause.MEASURING_CONTAINER
            }
            StatePause.CONTAINER -> {
                state.currentState = StatePause.MEASURING_CONTENT
            }
            else -> Unit
        }
        var lastFrameNanos = 0L
        while (true) {
            // state.isTransiting = true 用于稳住导航不要动，开始测量rect
            // state.isTransiting=true代表此时在打断动画中，rect都不为空，无需再次记录Frame标志
            // state.isTransiting=true时，两个rect一定不为空
            val frameNanos = withFrameNanos { it }
            var currentNanos = 0f
            if (lastFrameNanos != 0L) {
                currentNanos = (frameNanos - lastFrameNanos) / 1_000_000f
            }
            lastFrameNanos = frameNanos
            frameCount++
            val rect = if(isContainer) {
                state.containerRect
            } else {
                state.contentRect
            }
            if (rect != null) {
                // 避峰：如果仍超出16ms，则继续等帧直到不繁忙。危险操作！会降低响应度，应该酌情考虑
//                if(currentNanos > 16f) {
//                    LogUtil.warn("Pop : overdue 16ms(from frame${frameCount-1} to frame${frameCount} took ${currentNanos}ms),delay start transition")
//                    continue
//                }
                LogUtil.debug("Pop : waiting for $frameCount frame")
                return true
            }
            if (frameCount >= waitFrameMaxValue) {
                LogUtil.warn("Pop : rendering timeout after $frameCount frame")
                // 复位 停止等帧
                state.currentState = if(isContainer) {
                    StatePause.CONTAINER
                } else {
                    StatePause.CONTENT
                }
                return false
            }
        }
    }

    private suspend fun findState(
        key: String,
        onSwap: () -> Unit
    ) : SharedContainerState? {
        val state = states[key]
        if(state == null) {
            onSwap()
            return null
        }
        if(!waitContainerFrame(state,onSwap)) {
            return null
        }
        return state
    }

    suspend fun startPredictiveBack(
        key: String,
        onSwap: () -> Unit
    ) : SharedContainerState? {
        if(!enablePredictiveBack) {
            return null
        }
        val state = findState(key,onSwap) ?: return null
        snap(state,false)
        // 开始标识位
        resetOffset(state)
        state.currentState = StatePause.TRANSITING_TO_CONTAINER
        return state
    }

    suspend fun updatePredictiveBack(
        progress: Float,
        offset: Offset,
        state: SharedContainerState,
    ) {
        if(!enablePredictiveBack) {
            return
        }
        // 预测式时，content跟手位移，且画面保持按content原比例缩小，而不是直接调整进度
        state.contentOffset = offset
        state.animation.snapTo(progress)
    }


    suspend fun confirmPredictiveBack(
        state: SharedContainerState,
        onAnimatedFinished : (suspend () -> Unit)? = null,
    ) {
        if(!enablePredictiveBack) {
            return
        }
        state.currentState = StatePause.TRANSITING_TO_CONTAINER
        state.animation.animateTo(0f,getPopAnimation())
        onAnimatedFinished?.let { it() }
        resetOffset(state)
        state.contentRect = null
        // 结束标志位
        state.currentState = StatePause.CONTAINER
    }

    suspend fun cancelPredictiveBack(
        state: SharedContainerState,
        onAnimatedFinished: (suspend () -> Unit)? = null,
    ) = coroutineScope {
        if(!enablePredictiveBack) {
            return@coroutineScope
        }

        state.currentState = StatePause.TRANSITING_TO_CONTENT

        val job1 = launch {
            resetOffsetSmoothly(state)
        }

        val job2 = launch {
            state.animation.animateTo(
                1f,
                PredictiveUtil.cancelAnimation()
            )
        }

        joinAll(job1, job2)

        onAnimatedFinished?.invoke()

        state.containerRect = null
        // 结束标志位
        state.currentState = StatePause.CONTENT
    }

    fun clearStates() {
        states.clear()
        LogUtil.debug("Clear all SharedStates")
    }

    fun canPush(key: String) = states.contains(key) && enabled
    fun canPop() = enabled

    fun getRectInterpolator(): RectInterpolator {
        return if(screenRect == null || quadraticBezierRectInterpolatorVerticalRadio == 0f || quadraticBezierRectInterpolatorHorizontalRadio == 0f) {
            // 线性路径
            LinearRectInterpolator
        } else {
            // 二次贝塞尔路径
            QuadraticBezierRectInterpolator(
                screenRect!!,
                quadraticBezierRectInterpolatorVerticalRadio,
                quadraticBezierRectInterpolatorHorizontalRadio,
            )
        }
    }
}
