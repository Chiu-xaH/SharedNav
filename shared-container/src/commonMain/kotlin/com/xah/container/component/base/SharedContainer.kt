package com.xah.container.component.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sharednav.common.helper.ScreenCornerHelper
import com.sharednav.common.util.LogUtil
import com.xah.container.model.ContainerFilledStrategy
import com.xah.container.model.ContentStrategy
import com.xah.container.model.StatePause
import com.xah.container.util.LocalSharedRegistrySafely

fun Modifier.sharedContainer(
    key : String?,
    shape : CornerBasedShape,
    containerColor : Color?,
    shadow : Dp = 0.dp,
    enableQuadraticCorner : Boolean = false,
): Modifier {
    return this
        .shadow(shadow,shape)
        .clip(shape)
        .sharedContainer(
            key,
            if(containerColor == null) {
                ContainerFilledStrategy.Pixel(ContainerFilledStrategy.Clip)
            } else {
                ContainerFilledStrategy.Pixel(ContainerFilledStrategy.Color(containerColor))
            },
            shape,
            enableQuadraticCorner
        )
}


fun Modifier.sharedContainer(
    key : String?,
    shape : CornerBasedShape,
    shadow : Dp = 0.dp,
    containerFilledStrategy : ContainerFilledStrategy = ContainerFilledStrategy.Pixel(),
    enableQuadraticCorner : Boolean = false,
): Modifier {
    return this
        .shadow(shadow,shape)
        .clip(shape)
        .sharedContainer(key,containerFilledStrategy,shape,enableQuadraticCorner)
}

private fun Modifier.sharedContainer(
    key : String?,
    containerFilledStrategy : ContainerFilledStrategy,
    shape : CornerBasedShape,
    enableQuadraticCorner : Boolean
): Modifier = composed {
    if(key == null) {
        return@composed this
    }
    val registry = LocalSharedRegistrySafely.current ?: return@composed this
    if(!registry.enabled) {
        return@composed this
    }
    val state = remember { registry.register(key) }
    val contentStrategy = state.contentStrategy

    val graphicsLayer = rememberGraphicsLayer()
    val graphicsLayerForPixel = if(
        containerFilledStrategy.getFinalStrategy(registry) is ContainerFilledStrategy.Pixel
    ) {
        rememberGraphicsLayer()
    } else {
        null
    }

    LaunchedEffect(enableQuadraticCorner) {
        state.enableQuadraticCorner = enableQuadraticCorner
    }

    LaunchedEffect(shape) {
        state.containerCorner = shape
    }

    DisposableEffect (Unit) {
        LogUtil.debug("SharedContainer ${state.key} onCreate")
        state.isActive++
        state.containerFilledStrategy = containerFilledStrategy
        state.containerLayerForPixel = graphicsLayerForPixel
        state.containerLayer = graphicsLayer
        onDispose {
            LogUtil.debug("SharedContainer ${state.key} onDestroy")
            state.isActive--
        }
    }
    val useCopy = contentStrategy is ContentStrategy.Shared && contentStrategy.keepShowContainer
    fun Modifier.recordLayerAndAlpha() : Modifier = this
        .let { sub ->
            if(useCopy) {
                val progressOfAlpha = (state.animation.value * registry.speedUpRadioAlpha).coerceIn(0f,1f)
                sub.graphicsLayer(alpha = progressOfAlpha)
            } else {
                sub
            }
        }
        .drawWithContent {
            graphicsLayerForPixel?.record {
                this@drawWithContent.drawContent()
            }
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }
            if(useCopy) {
                drawContent()
            }
        }

    return@composed this
        .let {
            when(state.currentState) {
                StatePause.CONTAINER -> {
                    it.drawWithContent {
                        drawContent()
                    }
                }
                StatePause.CONTENT -> {
                    if(contentStrategy is ContentStrategy.Shared && !contentStrategy.keepShowContainer) {
                        it.drawWithContent {}
                    } else {
                        it
                    }
                }
                StatePause.MEASURING_CONTAINER -> {
                    if(!useCopy) {
                        it.graphicsLayer(alpha = 0f)
                    } else {
                        it
                    }
                        .drawWithContent {
                            drawContent()
                        }
                }
                StatePause.TRANSITING_TO_CONTENT -> {
                    it.recordLayerAndAlpha()
                }
                StatePause.TRANSITING_TO_CONTAINER -> {
                    it.recordLayerAndAlpha()
                }
                else -> {
                    it
                }
            }
        }
        // 记录两个组件的位置、大小
        .onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            val size = coordinates.size
            val layoutRect = Rect(
                left = position.x,
                top = position.y,
                right = position.x + size.width,
                bottom = position.y + size.height
            )
            state.containerRect = layoutRect
        }
}


private fun isOutOfScreen(screenRect : Rect,containerRect : Rect) : Boolean {
    return  containerRect.left < screenRect.left ||
            containerRect.top < screenRect.top ||
            containerRect.right > screenRect.right ||
            containerRect.bottom > screenRect.bottom
}


fun Modifier.sharedContent(
    key : String?,
    shape: CornerBasedShape,
    contentStrategy: ContentStrategy = ContentStrategy.Navigation(),
): Modifier {
    return this
        .let {
            when(contentStrategy) {
                is ContentStrategy.Shared -> {
                    it.clip(shape)
                }
                is ContentStrategy.Navigation -> {
                    it
                }
            }
        }
        .mSharedContent(key,shape,contentStrategy)
}

private fun Modifier.mSharedContent(
    key : String?,
    shape: CornerBasedShape,
    contentStrategy: ContentStrategy,
): Modifier = composed {
    if(key == null) {
        return@composed this
    }
    val registry = LocalSharedRegistrySafely.current ?: return@composed this
    if(!registry.enabled) {
        return@composed this
    }

    val state = remember { registry.get(key,contentStrategy) }
    if(state == null) {
        return@composed this
    }
    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(shape) {
        state.contentCorner = shape
    }

    DisposableEffect (Unit) {
        state.contentLayer = graphicsLayer
        onDispose {}
    }

    fun Modifier.recordLayer() : Modifier = this.drawWithContent {
        graphicsLayer.record {
            this@drawWithContent.drawContent()
        }
    }

    this
        .let {
            when(state.currentState) {
                StatePause.CONTENT -> {
                    it
                }
                StatePause.MEASURING_CONTENT -> {
                    it
                        .let {
                            if(contentStrategy is ContentStrategy.Shared && contentStrategy.keepShowContainer) {
                                it
                            } else {
                                it.graphicsLayer(alpha = 0f)
                            }
                        }
                        .drawWithContent {
                            drawContent()
                        }
                }
                StatePause.TRANSITING_TO_CONTAINER -> {
                    it.recordLayer()
                }
                StatePause.TRANSITING_TO_CONTENT -> {
                    it.recordLayer()
                }
                else -> {
                    it
                }
            }
        }
        // 记录组件的位置、大小
        .onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot()
            val size = coordinates.size

            state.contentRect = Rect(
                left = position.x,
                top = position.y,
                right = position.x + size.width,
                bottom = position.y + size.height
            )
        }
}

/**
 * 共享容器的内容
 * @param key 两个容器之间的Key
 * @param shape 屏幕圆角
 */
@Composable
fun SharedContent(
    key : String?,
    modifier : Modifier = Modifier,
    shape : CornerBasedShape = RoundedCornerShape(ScreenCornerHelper.corner),
    contentStrategy: ContentStrategy = ContentStrategy.Navigation(),
    content : @Composable () -> Unit
)  {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.sharedContent(key,shape,contentStrategy)
        ) {
            content()
        }
    }
}


/** 共享容器的容器
 * @param key 两个容器之间的Key
 * @param containerFilledStrategy 容器填充策略
 * @param shape 容器圆角
 */
@Composable
fun SharedContainer(
    key : String?,
    shape: CornerBasedShape,
    modifier : Modifier = Modifier,
    shadow : Dp = 0.dp,
    containerFilledStrategy : ContainerFilledStrategy = ContainerFilledStrategy.Pixel(),
    enableQuadraticCorner : Boolean = false,
    content : @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(shadow,shape)
            .clip(shape)
    ) {
        Box(
            modifier = Modifier.sharedContainer(key, containerFilledStrategy, shape,enableQuadraticCorner)
        ) {
            content()
        }
    }
}

/** 共享容器的容器
 * @param key 两个容器之间的Key
 * @param containerColor 优先使用底部1像素填充，SDK低于33时若containerColor为null则使用填充方案，否则使用containerColor填充
 * @param shape 容器圆角
 */
@Composable
fun SharedContainer(
    key : String?,
    shape : CornerBasedShape,
    containerColor : Color?,
    modifier : Modifier = Modifier,
    shadow : Dp = 0.dp,
    enableQuadraticCorner : Boolean = false,
    content : @Composable () -> Unit
) = SharedContainer(
    key,
    shape,
    modifier,
    shadow,
    if(containerColor == null) {
        ContainerFilledStrategy.Pixel(ContainerFilledStrategy.Clip)
    } else {
        ContainerFilledStrategy.Pixel(ContainerFilledStrategy.Color(containerColor))
    },
    enableQuadraticCorner,
    content,
)