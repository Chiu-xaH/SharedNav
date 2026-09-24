package com.xah.navigation.controller

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import com.sharednav.common.manager.AnimationSpecManager
import com.sharednav.common.helper.EnableHelper
import com.sharednav.common.util.LogUtil
import com.sharednav.common.util.PredictiveUtil
import com.xah.container.controller.SharedRegistry
import com.xah.container.model.SharedContainerState
import com.xah.navigation.anim.effect.DefaultLevelNoneTransitionEffect
import com.xah.navigation.model.action.ActionType
import com.xah.navigation.model.action.LaunchMode
import com.xah.navigation.model.anim.EffectLevel
import com.xah.navigation.model.anim.TransitionEffect
import com.xah.navigation.model.anim.TransitionEntry
import com.xah.navigation.model.dest.Destination
import com.xah.navigation.model.dest.StackEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.pow

class NavigationController(
    private val scope: CoroutineScope,
    val startDestination: Destination,
    private val _stack: SnapshotStateList<StackEntry> = mutableStateListOf(),
    val sharedTransitionEffect: TransitionEffect,
    var sharedRegistry : SharedRegistry? = null,
    /**
     * 全局工作模式
     * - 为true则所有栈内页面都UI存活，只是被盖住了。优点：不需要手动保持State，代码管理简单。缺点：页面过多时会卡顿甚至OOM。
     * - 为false则在非动画时只保持栈顶单页面存活，动画时只保持双页面存活，其余页面均被销毁，通过SaveableStateHolder对状态进行保存，缺点：POP时如果上一个页面太重，会影响动画；需要手动使用rememberSaveable等方式保持State，代码管理复杂。优点：内存占用低，不会OOM。
     *
     * 即使为false的时候，也可以通过在Push时传入keepPreviousAlive=true，保活上一个页面，UI不会被销毁，所以推荐使用全局false，需要时在Push借助keepPreviousAlive单独豁免。
     *
     * 没有放置在 by mutableStateOf()，因为切换全局工作模式是一个非常不常用的操作，应该在初始化的时候就指定好，如确实有这个实时切换的需求可提issue。
     *
     */
    var enableKeepAlive : Boolean = false
) {
    val stack: List<StackEntry> get() = _stack
    var transitionEntry by mutableStateOf<TransitionEntry?>(null)
        private set

    var isTransitioning by mutableStateOf(false)
        private set

    var transitionLevel by mutableStateOf(EffectLevel.HIGH)
    var levelNoneTransitionEffect by mutableStateOf(DefaultLevelNoneTransitionEffect)
    var defaultTransitionEffect by mutableStateOf(sharedTransitionEffect)

    var enableBlur by mutableStateOf(EnableHelper.canBlur)
    var enableShader by mutableStateOf(EnableHelper.canShader)
    var enablePredictiveBack by mutableStateOf(EnableHelper.canPredictedGesture)
    /**
     * 允许Destination.PlaceHolder生效，如果Destination.enforcePlaceHolder为true则不受enableSplashScreen限制
     */
    var enableSplashScreen by mutableStateOf(false)

    /**
     * TODO 暂未上线 没写完
     * 是否允许在预测式手势时，背景也跟随手指进行进度变化，否则将恒为1f直到松手才开始变化
     */
    internal var enablePredictiveBackBackgroundFollow by mutableStateOf(false)

    val transitionProgress = Animatable(0f)

    companion object {
        private const val NS_PER_MS = 1_000_000L
        const val DEFAULT_SHARED_MAX_PRECENT = 0.875f

        /**
         * 前调快，后调慢。仅适合作为全屏页面的转场动画，显得不拖沓。其他场景可以先试试默认的FastOutSlowInEasing
         */
        val DEFAULT_EASING = CubicBezierEasing(0.4f, 0.65f, 0.25f, 1.0f)
    }

    private fun popAnimationWithShared() = tween<Float>(AnimationSpecManager.getSharedTween()*7/5)
    private fun pushAnimationWithShared() = tween<Float>(AnimationSpecManager.getSharedTween())

    internal var inPredictive by mutableStateOf(false)

    private fun getAnimation() =
        if(transitionLevel != EffectLevel.NONE && sharedRegistry?.isRunning == true) {
            when(transitionEntry!!.type) {
                ActionType.POP -> popAnimationWithShared()
                ActionType.PUSH -> pushAnimationWithShared()
            }
        } else {
            val transitionMode = current().transitionMode

            val newPopAnimation = (transitionMode.popAnimation as? TweenSpec<Float>)?.let {
                tween(
                    durationMillis = AnimationSpecManager.getTween(it.durationMillis),
                    delayMillis = it.delay,
                    easing = it.easing
                )
            } ?: transitionMode.popAnimation
            val newPushAnimation = (transitionMode.pushAnimation as? TweenSpec<Float>)?.let {
                tween(
                    durationMillis = AnimationSpecManager.getTween(it.durationMillis),
                    delayMillis = it.delay,
                    easing = it.easing
                )
            } ?: transitionMode.pushAnimation

            when(transitionEntry!!.type) {
                ActionType.POP -> newPopAnimation
                ActionType.PUSH -> newPushAnimation
            }
        }

    private fun createAndPush(
        destination : Destination,
        effect : TransitionEffect,
        keepPreviousAlive : Boolean = enableKeepAlive
    ) : StackEntry {
        val newEntry = StackEntry(
            destination = destination,
            transitionMode = effect,
            keepPreviousAlive = keepPreviousAlive
        )
        _stack += newEntry
        return newEntry
    }

    private fun removeAndPop() : StackEntry? {
        if(canPop()) {
            return _stack.removeAt(_stack.size-1)
        }
        return null
    }

    private fun pushInternal(
        destination: Destination,
        launchMode: LaunchMode,
        effect: TransitionEffect
    ) {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            var cachedEntry : StackEntry? = null
            // 并行动画
            if(isTransitioning && transitionEntry?.type == ActionType.POP) {
                var reuse = launchMode.reuse
                if(launchMode is LaunchMode.PopToExisting && launchMode.actionType == ActionType.PUSH) {
                    reuse = true
                }

                if(reuse && isCurrentDestination(destination)) {
                    // 同一界面的打断，无需解除容器共享和重建栈
                    cachedEntry = removeAndPop()
                } else {
                    sharedRegistry?.cancelPop()
                    removeAndPop()
                }
            }

            val from = current()

            when (launchMode) {
                is LaunchMode.Push -> {
                    // 解析 keepPreviousAlive：null 时跟随 from.keepAlive，若栈空则跟随 enableKeepAlive
                    val keepPreviousAlive = launchMode.keepPreviousAlive ?: from.keepPreviousAlive

                    if(launchMode.reuse) {
                        // 如果栈顶是目标项目，则复用
                        if(isCurrentDestination(destination)) {
                            LogUtil.debug("Push(reuse=true) : current is target destination ${destination.key}")
                            // 如果栈顶就是目标，保持栈顶不变
                            return@launch
                        } else {
                            if(cachedEntry != null) {
                                LogUtil.debug("Push(reuse=true) : reuse destination ${destination.key}")
                                _stack.add(cachedEntry)
                            } else {
                                LogUtil.debug("Push(reuse=true) : create destination ${destination.key}")
                                createAndPush(destination,effect,keepPreviousAlive)
                            }
                        }
                    } else {
                        LogUtil.debug("Push(reuse=false) : create destination ${destination.key}")
                        // 每次都创建新的并加入栈
                        createAndPush(destination,effect,keepPreviousAlive)
                    }
                }
                is LaunchMode.Clear -> {
                    if(launchMode.reuse) {
                        // 栈内存在则复用并清空其余项，没有则直接CLEAR_STACK
                        // 从栈底（索引0）开始寻找
                        val existingIndex = _stack.indexOfFirst { it.destination == destination }
                        if(existingIndex != -1) {
                            LogUtil.debug("Single(reuse=true) : found destination ${destination.key}")
                            // 目标项已经存在，复用
                            val item = _stack[existingIndex]
                            _stack.clear()
                            _stack.add(item)
                        } else {
                            LogUtil.debug("Single(reuse=true) : not found destination ${destination.key}")
                            // 如果栈中没有该目标，直接清空栈并压入
                            createAndPushClearly(destination,effect)
                        }
                    } else {
                        LogUtil.debug("Single(reuse=false) : create destination ${destination.key}")
                        // 清空栈并压入
                        createAndPushClearly(destination,effect)
                    }
                }
                is LaunchMode.PopToExisting -> {
                    if(launchMode.reuse) {
                        // 如果栈顶就是目标，保持栈顶不变
                        if(isCurrentDestination(destination)) {
                            LogUtil.debug("PopToExisting(reuse=true) : current is target destination ${destination.key}")
                            return@launch
                        }
                        // 如果栈中已经有该目标，则清除其之上的所有栈并复用它
                        if(previous()?.destination == destination) {
                            // 等效于POP
                            LogUtil.debug("PopToExisting(reuse=true) : equal Pop ${destination.key}")
                            pop()
                            return@launch
                        }
                        val existingIndex = _stack.indexOfFirst { it.destination == destination }
                        if(existingIndex != -1) {
                            LogUtil.debug("PopToExisting(reuse=true) : found destination ${destination.key}")
                            // 清除中间元素
                            _stack.subList(existingIndex + 1, _stack.size-1).clear()
                            popInternal()
                            return@launch
                        } else {
                            LogUtil.debug("PopToExisting(reuse=true) : not found destination ${destination.key}")
                            launchMode.actionType = ActionType.PUSH
                            createAndPush(destination,effect)
                        }
                    } else {
                        // 如果栈中已经有该目标，则清除其之上的所有栈(包括自己)并重新创建
                        val existingIndex = _stack.indexOfFirst { it.destination == destination }
                        if(existingIndex != -1) {
                            LogUtil.debug("PopToExisting(reuse=false) : found destination ${destination.key}")
                            // 清除中间元素
                            _stack.subList(existingIndex + 1, _stack.size-1).clear()
                            previous()?.resetState() ?: return@launch
                            popInternal()
                            return@launch
                        } else {
                            LogUtil.debug("PopToExisting(reuse=false) : not found destination ${destination.key}")
                            launchMode.actionType = ActionType.PUSH
                            createAndPush(destination,effect)
                        }
                    }
                }
                is LaunchMode.Replace -> {
                    if(launchMode.reuse) {
                        // 如果栈顶是目标项目，则复用
                        if(isCurrentDestination(destination)) {
                            LogUtil.debug("Replace(reuse=true) : current is target destination ${destination.key}")
                            // 如果栈顶就是目标，保持栈顶不变
                            return@launch
                        } else {
                            if(cachedEntry != null) {
                                LogUtil.debug("Replace(reuse=true) : reuse destination ${destination.key}")
                                removeAndPop()
                                _stack.add(cachedEntry)
                            } else {
                                LogUtil.debug("Replace(reuse=true) : create destination ${destination.key}")
                                removeAndPop()
                                createAndPush(destination,effect)
                            }
                        }
                    } else {
                        LogUtil.debug("Replace(reuse=false) : create destination ${destination.key}")
                        // 将栈顶替换为新的实例
                        removeAndPop()
                        createAndPush(destination,effect)
                    }
                }
            }
            // 动画未进行时归位，不影响打断动画
            val type = launchMode.actionType
            snap(type)
            // 添加过渡动画
            transitionEntry = TransitionEntry(
                type = type,
                from = from,
                to = current(),
                effect = effect
            )
        }
    }

    private fun popInternal() {
        scope.launch(start = CoroutineStart.UNDISPATCHED) {

            if(!canPop()) {
                return@launch
            }

            val from = current()
            val to = previous() ?: return@launch

            val type = ActionType.POP

            snap(type)

            transitionEntry = TransitionEntry(
                type = type,
                from = from,
                to = to,
                effect = from.transitionMode
            )
        }
    }

    // 动画未进行时归位，不影响打断动画
    private suspend fun snap(
        type : ActionType
    ) {
        if(!isTransitioning) {
            transitionProgress.snapTo(
                when(type) {
                    ActionType.PUSH -> 0f
                    ActionType.POP -> 1f
                }
            )
        }
    }

    fun animate() {
        scope.launch {
            internalAnimate()
        }
    }


    private suspend fun internalAnimate() {
        if(sharedRegistry?.isWaitingFrame == true) {
            return
        }
        transitionEntry ?: return

        val target = when (transitionEntry!!.type) {
            ActionType.PUSH -> 1f
            ActionType.POP -> 0f
        }

        // 设置标志位，开始动画
        waitFrame(transitionEntry!!) {
            isTransitioning = true
        }
        transitionProgress.animateTo(targetValue = target, animationSpec = getAnimation())

        // 移除栈，置状态
        if(transitionEntry?.type == ActionType.POP) {
            removeAndPop()
        }
        resetTransitionState()
    }

    private fun canShared() = transitionLevel != EffectLevel.NONE && sharedRegistry != null

    /**
     * @param destination 目标页面
     * @param launchMode 启动模式。默认为栈顶复用
     */
    fun push(
        destination: Destination,
        launchMode: LaunchMode = LaunchMode.Push(),
    ) = push(
        destination,
        launchMode,
        defaultTransitionEffect
    )

    /**
     * @param destination 目标页面
     * @param launchMode 启动模式。默认为栈顶复用
     * @param effect 转场动效。当effectLevel为NONE时，强制使用levelNoneTransitionEffect，传参无效。当可容器共享时，强制使用sharedTransitionEffect，传参无效；
     */
    fun push(
        destination: Destination,
        launchMode: LaunchMode = LaunchMode.Push(),
        effect: TransitionEffect
    ) {
        if(
            canShared() &&
            launchMode.actionType == ActionType.PUSH &&
            sharedRegistry!!.canPush(destination.key)
        ) {
            sharedRegistry!!.push(
                destination.key,
                onAnimatedFinished = { awaitTransition() },
            ) {
                pushInternal(destination,launchMode,sharedTransitionEffect)
            }
        } else {
            val finalEffect = if(transitionLevel == EffectLevel.NONE) {
                levelNoneTransitionEffect
            } else {
                effect
            }
            pushInternal(destination,launchMode,finalEffect)
        }
    }

    fun pop() {
        var key = getCurrentSharedKey()
        // 并行动画
        if(
            isTransitioning &&
            transitionEntry?.type == ActionType.POP &&
            // 只剩一个页面了，再返回就退出应用了，没必要再执行并行动画了
            _stack.size > 2
        ) {
            removeAndPop()
            resetTransitionState()
            if(sharedRegistry?.cancelPop() != null) {
                key = getCurrentSharedKey()
            }
        }

        if(
            canShared() &&
            sharedRegistry!!.canPop()
        ) {
            sharedRegistry!!.pop(
                key,
                onAnimatedFinished = { awaitTransition() }
            ) {
                popInternal()
            }
        } else {
            popInternal()
        }
    }

    private fun getCurrentSharedKey() = current().destination.key

    /**
     * 延迟等动画结束后再加载内容，适合例如Bitmap、Video等
     */
    suspend fun awaitTransition() = snapshotFlow { isTransitioning }.filter { !it }.first()

    fun current() : StackEntry = _stack.last()

    val current by derivedStateOf { _stack.last() }

    fun currentDestination() : Destination = current().destination

    val currentDestination by derivedStateOf { _stack.last().destination }

    fun isCurrentDestination(destination : Destination) : Boolean = currentDestination() == destination

    /**
     * n=1时为上一个
     * n=2时为上上个
     * ...
     */
    fun previous(n : Int = 1) : StackEntry? = _stack.getOrNull(_stack.lastIndex - n)

    val previous by derivedStateOf { _stack.getOrNull(_stack.lastIndex - 1) }

    /**
     * n=1时为上一个
     * n=2时为上上个
     * ...
     */
    fun previousDestination(n : Int = 1) : Destination? = previous(n)?.destination

    val previousDestination by derivedStateOf { _stack.getOrNull(_stack.lastIndex - 1)?.destination }

    /**
     * 寻找上一个非存活态导航
     * 从后向前找，直到器keepAlive=false返回
     */
    fun previousNotAlive(): StackEntry? {
        for (i in _stack.lastIndex - 1 downTo 1) {
            val entry = _stack[i]
            if (!entry.keepPreviousAlive) {
                return _stack[i-1]
            }
        }
        return null
    }

    val previousNotAlive by derivedStateOf {
        for (i in _stack.lastIndex - 1 downTo 1) {
            val entry = _stack[i]
            if (!entry.keepPreviousAlive) {
                return@derivedStateOf _stack[i-1]
            }
        }
        return@derivedStateOf null
    }

    fun previousNotAliveDestination() : Destination? = previousNotAlive()?.destination

    val previousNotAliveDestination by derivedStateOf {
        for (i in _stack.lastIndex - 1 downTo 1) {
            val entry = _stack[i]
            if (!entry.keepPreviousAlive) {
                return@derivedStateOf _stack[i-1].destination
            }
        }
        return@derivedStateOf null
    }

    fun canPop() : Boolean = _stack.size > 1

    val canPop by derivedStateOf { _stack.size > 1 }

    private fun contains(destination: Destination) : StackEntry? {
        return _stack.find { it.destination == destination }
    }

    fun containsDestination(destination: Destination) : Boolean {
        return contains(destination) != null
    }

    // 清空栈并压入
    private fun createAndPushClearly(
        destination : Destination,
        effect: TransitionEffect
    ) {
        _stack.clear()
        createAndPush(destination,effect)
    }

    suspend fun startPredictiveBackShared() : SharedContainerState? {
        if(canShared()) {
            return sharedRegistry!!.startPredictiveBack(
                current().destination.key,
            ) {
                startPredictiveBack()
            }
        } else {
            startPredictiveBack()
            return null
        }
    }

    fun startPredictiveBackSharedAsync(
        onResult: (SharedContainerState?) -> Unit
    ) {
        scope.launch {
            val result = startPredictiveBackShared()
            onResult(result)
        }
    }

    private fun startPredictiveBack() {
        scope.launch {
            if(!canPop()) {
                return@launch
            }
            val from = current()
            val to = previous() ?: return@launch
            snap(ActionType.POP)
            transitionEntry = TransitionEntry(
                type = ActionType.POP,
                from = from,
                to = to,
                effect = from.transitionMode
            )
            inPredictive = true
            isTransitioning = true
        }
    }

    private fun dampOffset(offset: Offset, factor: Float = 0.01f): Offset {
        val distance = offset.getDistance()
        if(distance == 0f) {
            return offset
        }

        val scale = 1f / (1f + distance * factor)
        return offset * scale
    }

    fun updatePredictiveBackShared(
        progress: Float,
        offset: Offset,
        state : SharedContainerState?
    ) {
        scope.launch {
            val canShared = state != null && canShared()
            val easedContainer = getPredictiveMaxValue(progress)

            if(canShared) {
                launch {
                    sharedRegistry!!.updatePredictiveBack(easedContainer, dampOffset(offset), state)
                }
            }
            launch {
                updatePredictiveBack(
                    // 有容器的时候背景不动
//                    if(enablePredictiveBackBackgroundFollow) easedContainer else 1f
                    if(canShared) {
                        1f
                    } else {
                        easedContainer
                    }
                )
            }
        }
    }

    fun getPredictiveMaxValue(
        progress : Float
    ) : Float {
        val minValue = current().transitionMode.predictiveMinValue
        val easedContainer = 1f - ((1f - minValue) * progress.pow(0.5f))
        return easedContainer
    }

    private fun updatePredictiveBack(
        progress: Float,
    ) {
        scope.launch {
            transitionProgress.snapTo(progress)
        }
    }


    private fun confirmPredictiveBack() {
        scope.launch {
            inPredictive = false
            transitionProgress.animateTo(0f, getAnimation())
            removeAndPop()
            resetTransitionState()
        }
    }

    fun confirmPredictiveBackShared(
        state : SharedContainerState?
    ) {
        val canShared = state != null && canShared()
        scope.launch {
            if(canShared) {
                launch {
                    sharedRegistry!!.confirmPredictiveBack(state) {
                        awaitTransition()
                    }
                }
            }
            launch { confirmPredictiveBack() }
        }
    }

    private fun cancelPredictiveBack() {
        scope.launch {
            transitionProgress.animateTo(1f, PredictiveUtil.cancelAnimation())
            resetTransitionState()
            inPredictive = false
        }
    }

    fun cancelPredictiveBackShared(
        state : SharedContainerState?
    ) {
        val canShared = state != null && canShared()
        scope.launch {
            if(canShared) {
                launch {
                    sharedRegistry!!.cancelPredictiveBack(state) {
                        awaitTransition()
                    }
                }
            }
            launch { cancelPredictiveBack() }
        }
    }

    /**
     * 为保证界面创建的时候，isTransitioning马上为true，完成后置为false，供开发者监听
     */
    internal fun setTransiting() {
        // 无动效时无需，例如第一个页面创建
        if(transitionEntry == null) {
            return
        }
        isTransitioning = true
    }

    private fun resetTransitionState() {
        transitionEntry = null
        isTransitioning = false
    }

    var maxWaitFrame = 10

    /**
     * 流畅度优化
     */
    private suspend fun waitFrame(
        entry: TransitionEntry,
        onSwap: () -> Unit,
    ) {
        // 一定要确保页面切换(onSwap)之后马上等帧(awaitFrame)
        onSwap()
        var frameCount = 0
        var curTime = 0L
        while (frameCount < maxWaitFrame) {
            val prevTime = curTime
            withFrameNanos { curTime = it }
            frameCount++
            val time = curTime - prevTime
            if(
                // 首次
                prevTime > 0L &&
                time <= 16 * NS_PER_MS
                // 空闲 判断帧间隔是否小于等于16ms
            ) {
                LogUtil.debug("${entry.type.name} to ${entry.to.destination.key} : waited for $frameCount frame ${time.toDouble()/NS_PER_MS}ms")
                return
            } else {
                LogUtil.warn("${entry.type.name} to ${entry.to.destination.key} : waiting for $frameCount frame ${time.toDouble()/NS_PER_MS}ms")
            }
        }
    }

    init {
        if(_stack.isEmpty()) {
            createAndPush(startDestination,defaultTransitionEffect)
        }
    }
}