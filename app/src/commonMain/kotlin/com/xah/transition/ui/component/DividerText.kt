package com.xah.transition.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.xah.container.component.base.SharedContainer
import com.xah.container.component.base.SharedContent
import com.xah.container.model.ContentStrategy
import com.xah.container.util.LocalSharedRegistry
import com.sharednav.common.helper.NoneRoundShape
import com.xah.container.component.base.sharedContainer

val ANIMATION_SPEED = 400
val DIVIDER_TEXT_VERTICAL_PADDING = 9.dp
// 小标题
@Composable
fun DividerText(
    text: String,
    style : TextStyle = LocalTextStyle.current,
    contentColor : Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit?)? = null
) = DividerText(
    text = text,
    style = style,
    contentColor = contentColor,
    onClick = onClick,
    modifier = Modifier
)

@Composable
private fun DividerText(
    text: String,
    modifier: Modifier = Modifier,
    style : TextStyle = LocalTextStyle.current,
    contentColor : Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit?)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f, // 按下时为0.9，松开时为1
        animationSpec = tween(ANIMATION_SPEED / 2, easing = LinearOutSlowInEasing),
        label = "" // 使用弹簧动画
    )
    val color by animateColorAsState(
        targetValue = if (isPressed) contentColor.copy(alpha = 0.7f) else contentColor,
        label = ""
    )

    Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier
            .padding(horizontal = APP_HORIZONTAL_DP , vertical = DIVIDER_TEXT_VERTICAL_PADDING)
            .clickable { onClick?.invoke() }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick?.invoke()
                    }
                )
            }
            .indication(interactionSource = remember { MutableInteractionSource() }, indication = null)
            .scale(scale.value)
    )
}


// 按压小标题展开/收起下面内容
@Composable
fun DividerTextExpandedWithShared(
    text : String,
    style : TextStyle = LocalTextStyle.current,
    shape : RoundedCornerShape = NoneRoundShape,
    contentColor : Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val registry = LocalSharedRegistry.current
    var expanded by rememberSaveable() { mutableStateOf(true) }
    val key = remember(text) { "divider_text_$text" }

    fun set() {
        if(expanded) {
            registry.pop(key) {
                expanded = !expanded
            }
        } else {
            registry.push(key) {
                expanded = !expanded
            }
        }
    }


    DividerText(
        text = text,
        modifier = Modifier.sharedContainer(key,NoneRoundShape),
        style = style,
        contentColor = contentColor,
        onClick = { set() },
    )

    AnimatedVisibility(
        enter = scaleIn(animationSpec = registry.getPushAnimation()) + expandIn(expandFrom = Alignment.BottomCenter,animationSpec = registry.getPushAnimation()),
        exit = scaleOut(animationSpec = registry.getPopAnimation()) + shrinkOut(shrinkTowards = Alignment.BottomCenter,animationSpec = registry.getPopAnimation()),
        visible = expanded,
    ) {
        SharedContent(
            key = key,
            contentStrategy = ContentStrategy.Shared(keepShowContainer = true,enableContainerAlpha = true),
            shape = shape,
        ) {
            Column {
                content()
            }
        }
    }
}


