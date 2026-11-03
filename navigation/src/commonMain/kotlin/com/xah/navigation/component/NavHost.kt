package com.xah.navigation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sharednav.common.modifier.touchEvent
import com.sharednav.common.util.LogUtil
import com.xah.container.component.base.SharedContent
import com.xah.container.component.overlay.SharedContainerRoot
import com.xah.container.controller.SharedRegistry
import com.xah.container.util.LocalSharedRegistry
import com.xah.floating.component.FloatingRoot
import com.xah.navigation.anim.backgroundEffect
import com.xah.navigation.anim.effect.DefaultTransitionEffect
import com.xah.navigation.anim.effect.rememberDefaultPageEffects
import com.xah.navigation.anim.foregroundEffect
import com.xah.navigation.anim.innerEffect
import com.xah.navigation.controller.NavigationController
import com.xah.navigation.controller.NavigationViewModel
import com.xah.navigation.model.action.ActionType
import com.xah.navigation.model.anim.TransitionEffect
import com.xah.navigation.model.dest.Dependencies
import com.xah.navigation.model.dest.Destination
import com.xah.navigation.model.dest.StackEntry
import com.xah.navigation.util.DefaultBackHandler
import com.xah.navigation.util.LocalNavController
import com.xah.navigation.util.LocalNavControllerSafely
import com.xah.navigation.util.LocalNavDependencies
import kotlin.collections.associateBy

@Composable
fun rememberNavController(
    startDestination : Destination,
    sharedTransitionEffect: TransitionEffect = DefaultTransitionEffect(rememberDefaultPageEffects())
): NavigationController {
    val scope = rememberCoroutineScope()
    val navViewModel: NavigationViewModel = viewModel(factory = NavigationViewModel.Factory())
    val navController = remember(navViewModel) {
        NavigationController(
            scope,
            startDestination,
            navViewModel.stack,
            sharedTransitionEffect,
            null
        )
    }
    return navController
}

@Composable
fun SharedNavHost(
    navController: NavigationController,
    modifier: Modifier = Modifier,
    dependencies: Dependencies = Dependencies(),
    backHandler: (@Composable () -> Unit) = { DefaultBackHandler() },
) {
    SharedContainerRoot {
        val registry = LocalSharedRegistry.current
        CompositionLocalProvider(
            LocalNavControllerSafely provides navController,
            LocalNavController provides navController,
        ) {
            FloatingRoot(registry = registry) {
                NavHost(
                    navController,
                    registry,
                    modifier,
                    dependencies,
                    backHandler
                )
            }
        }
    }
}

@Composable
private fun NavHost(
    navController: NavigationController,
    registry: SharedRegistry,
    modifier: Modifier = Modifier,
    dependencies: Dependencies = Dependencies(),
    backHandler: (@Composable () -> Unit) = { DefaultBackHandler() },
) {
    val enableKeepAlive = navController.enableKeepAlive
    val saveableStateHolder = if(!enableKeepAlive) {
        rememberSaveableStateHolder()
    } else {
        null
    }

    LaunchedEffect(registry) {
        navController.sharedRegistry = registry
    }

    ScreenOrientationEffect()

    CompositionLocalProvider(
        LocalNavControllerSafely provides navController,
        LocalNavController provides navController,
        LocalNavDependencies provides dependencies,
    ) {
        backHandler()

        val transitionEntry = navController.transitionEntry
        val progress = navController.transitionProgress

        // 当 transition 变化时启动动画
        LaunchedEffect(
            transitionEntry,
            registry.isRunning,
            registry.isWaitingFrame
        ) {
            navController.animate()
        }

        val visibleEntries = if (enableKeepAlive) {
            // 全栈模式 KEEP_ALIVE
            val result = navController.stack
            when (transitionEntry?.type) {
                ActionType.POP -> result + listOf(transitionEntry.to, transitionEntry.from)
                ActionType.PUSH -> result + listOf(transitionEntry.from, transitionEntry.to)
                else -> navController.stack
            }
        } else {
            // 单栈模式 SAVE_STATE
            /*
            when (transitionEntry?.type) {
                ActionType.POP -> listOf(transitionEntry.to, transitionEntry.from)
                ActionType.PUSH -> listOf(transitionEntry.from, transitionEntry.to)
                else -> listOf(navController.stack.last())
            }
             */
            val compositedEntries = navController.stack
                .filter { it.keepPreviousAlive }
                .mapNotNull { entry ->
                    navController.stack.getOrNull(navController.stack.indexOf(entry) - 1)
                }

            when (transitionEntry?.type) {
                ActionType.POP -> {
                    val base = mutableListOf(transitionEntry.to, transitionEntry.from)
                    compositedEntries
                        .filter { it !in base }
                        .forEach { base.add(0, it) }
                    base
                }
                ActionType.PUSH -> {
                    val base = mutableListOf(transitionEntry.from, transitionEntry.to)
                    compositedEntries
                        .filter { it !in base }
                        .forEach { base.add(0, it) }
                    base
                }
                else -> {
                    val top = navController.stack.last()
                    val base = mutableListOf(top)
                    compositedEntries
                        .filter { it != top }
                        .forEach { base.add(0, it) }
                    base
                }
            }
        }
            // 去重兜底
            .distincted()

        val level = navController.transitionLevel
        val enableBlur = navController.enableBlur
        val enableShader = navController.enableShader

        @Composable
        fun Content(entry: StackEntry) {
            val isFrom = transitionEntry?.from == entry
            val isTo = transitionEntry?.to == entry

            val animatedProgress = progress.value

            val finalTransitionMode = transitionEntry?.effect ?: navController.defaultTransitionEffect
            val effect = finalTransitionMode.pageEffect

            val backgroundEffect = remember(animatedProgress,level) { effect.background(animatedProgress,level) }
            val foregroundEffect = remember(animatedProgress,level) { effect.foreground(animatedProgress, level) }

            // 当返回时，禁用前景；当前进时，禁用背景；当非动画态，启用
            val (enableTouch,interceptTouch) = when(transitionEntry?.type) {
                ActionType.POP -> {
                    Pair(isTo,false)
                }
                ActionType.PUSH -> {
                    Pair(false,true)
                }
                null -> {
                    Pair(true,true)
                }
            }

            val enableMirrorForBg = enableShader && effect.backgroundEffect.enableMirror
            val enableMirrorForFg = enableShader && effect.foregroundEffect.enableMirror
            val backgroundColor = effect.backgroundEffect.backgroundColor

            DisposableEffect(Unit) {
                // 为保证界面创建的时候，isTransitioning马上为true，完成后置为false，供开发者监听
                navController.setTransiting()
                LogUtil.debug("viewCreate: $entry; stack=${navController.stack.joinToString(",")}")

                onDispose {
                    // 界面销毁，一般不需要提供给开发者，开发者自己在自己的页面就可以监听，这里只给个日志供调试
                    LogUtil.debug("viewDestroy: $entry; stack=${navController.stack.joinToString(",")}")
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .let {
                        // 容器等帧测量时，禁用所有动效，测量容器的真实位置
                        if (transitionEntry != null) {
                            when (transitionEntry.type) {
                                ActionType.PUSH -> {
                                    if (isFrom) {
                                        // 背景
                                        return@let it.backgroundEffect(
                                            enableMirrorForBg,
                                            enableBlur,
                                            backgroundColor,
                                            backgroundEffect
                                        )
                                    }
                                    if (isTo) {
                                        // 目标屏幕
                                        if(!registry.isRunning) {
                                            return@let it.foregroundEffect(
                                                enableBlur,
                                                enableMirrorForFg,
                                                foregroundEffect,
                                            )
                                        }
                                    }
                                }
                                ActionType.POP -> {
                                    if (isTo) {
                                        // 背景
                                        return@let it.backgroundEffect(
                                            enableMirrorForBg,
                                            enableBlur,
                                            backgroundColor,
                                            backgroundEffect
                                        )
                                    }
                                    if (isFrom) {
                                        // 退出屏幕
                                        if(!registry.isRunning) {
                                            return@let it.foregroundEffect(
                                                enableBlur,
                                                enableMirrorForFg,
                                                foregroundEffect,
                                            )
                                        }
                                    }
                                }
                            }
                        } else if(navController.stack.last() != entry) {
                            // 仍生效背景效果
                            return@let it.backgroundEffect(
                                enableMirrorForBg,
                                enableBlur,
                                backgroundColor,
                                backgroundEffect
                            )
                        }
                        return@let it
                    }
                    .touchEvent(
                        enableTouch,interceptTouch
                    )
            ) {
                SharedContent(
                    key = entry.destination.key,
                    modifier = Modifier
                        .let {
                            // 容器等帧测量时，禁用所有动效，测量容器的真实位置
                            if (transitionEntry != null) {
                                when (transitionEntry.type) {
                                    ActionType.PUSH -> {
                                        if (isFrom) {
                                            // 背景
                                            return@let it.innerEffect(
                                                enableMirrorForBg,
                                                enableBlur,
                                                backgroundEffect
                                            )
                                        }
                                        if (isTo) {
                                            // 目标屏幕
                                            if(!registry.isRunning) {
                                                return@let it.innerEffect(
                                                    enableBlur,
                                                    enableMirrorForFg,
                                                    foregroundEffect,
                                                )
                                            }
                                        }
                                    }
                                    ActionType.POP -> {
                                        if (isTo) {
                                            // 背景
                                            return@let it.innerEffect(
                                                enableMirrorForBg,
                                                enableBlur,
                                                backgroundEffect
                                            )
                                        }
                                        if (isFrom) {
                                            // 退出屏幕
                                            if(!registry.isRunning) {
                                                return@let it.innerEffect(
                                                    enableBlur,
                                                    enableMirrorForFg,
                                                    foregroundEffect,
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if(navController.stack.last() != entry) {
                                // 仍生效背景效果
                                return@let it.innerEffect(
                                    enableMirrorForBg,
                                    enableBlur,
                                    backgroundEffect
                                )
                            }
                            return@let it
                        }
                ) {
                    val needDisplaySplashScreen = entry.destination.enforcePlaceHolder ?: navController.enableSplashScreen
                    // NONE等级动效不需要遮罩
                    val enableSplashScreen = needDisplaySplashScreen && entry.destination.PlaceHolder != null
                    // 动画过程中且为前景
                    val inTransiting = (transitionEntry?.type == ActionType.POP && isFrom && navController.isTransitioning) || (transitionEntry?.type == ActionType.PUSH && isTo)

                    if(enableSplashScreen && inTransiting && !navController.inPredictive) {
                        // SplashScreen
                        entry.destination.PlaceHolder!!.invoke()
                    } else {
                        entry.destination.Content()
                    }
                }
            }
        }

        Box(
            modifier = modifier.fillMaxSize()
        ) {
            visibleEntries.forEachIndexed { _, entry ->
                key(entry.id) {
                    // saveableStateHolder 变化时必须重建页面Content
                    saveableStateHolder?.SaveableStateProvider(entry.id) {
                        Content(entry)
                    }
                        ?: Content(entry)
                }
            }
        }
    }
}

private fun List<StackEntry>.distincted() = this.associateBy { it.id }.values.toList()