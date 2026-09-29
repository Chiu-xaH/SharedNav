package com.xah.transition.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.xah.navigation.model.action.LaunchMode
import com.xah.navigation.util.LocalNavController
import com.xah.transition.ui.screen.nav.destination.ControlCenterDestination
import com.xah.transition.ui.style.effect.ControlCenterTransitionEffect
import com.sharednav.common.kmp.LocalPlatformActivity
import com.xah.transition.ui.screen.nav.destination.base.NavDestination
import com.sharednav.common.kmp.PlatformActivity
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sharednav.app.generated.resources.Res
import sharednav.app.generated.resources.ic_arrow_back
import sharednav.app.generated.resources.ic_close

expect fun PlatformActivity.finishCurrentActivity()

/*
TODO
[UX]手指按住向右拖动调用预测式返回，反向松手取消预测式，继续松手执行返回
[UX]手指按住向下拖动跟手图标，模糊缩放背景，到一定程度显示ControlCenter，未达到阈值时松手返回，达到阈值后松手仍保持打开状态，点击空白区域或Icon关闭面板
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarNavigationIcon(
    tint : Color = MaterialTheme.colorScheme.primary,
    modifier : Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val navController = LocalNavController.current
    val activity = LocalPlatformActivity()
    val scope = rememberCoroutineScope()
    val queue = navController.stack.reversed()
    var displayDialog by remember { mutableStateOf(false) }

    if(displayDialog) {
        Dialog(
            onDismissRequest = { displayDialog = false }
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .clickable(
                    // 去掉水波纹
                    interactionSource = null,
                    indication = null
                ) {
                    displayDialog = false
                }
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(vertical = APP_HORIZONTAL_DP)
                        .align(Alignment.TopCenter)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f, fill = false),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        LazyColumn {
                            items(queue.size) { index ->
                                val item = queue[index]
                                val dest = item.destination as NavDestination
                                val title = dest.title
                                val isCurrent = index == 0
                                TransplantListItem(
                                    headlineContent = {
                                        Text(title ,fontWeight = if(isCurrent) FontWeight.Bold else FontWeight.Normal)
                                    },
                                    leadingContent = {
                                        Icon(
                                            painterResource(dest.icon),
                                            null
                                        )
                                    },
                                    trailingContent = {
                                        if(isCurrent) {
                                            Icon(painterResource(Res.drawable.ic_arrow_back),null)
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        if(isCurrent) {
                                            displayDialog = false
                                        } else {
                                            scope.launch {
                                                navController.push(item.destination, LaunchMode.PopToExisting())
                                                displayDialog = false
                                            }
                                        }
                                    }
                                )
                                if(index != queue.size-1) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val enabled = navController.canPop()
    var downDrag by remember { mutableFloatStateOf(0f) }
    var rightDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .padding(horizontal = CARD_NORMAL_DP/2)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        val dx = dragAmount.x
                        val dy = dragAmount.y

                        // 下滑唤醒启动台 TODO 后期做跟手
                        if (dy > 0 && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                            downDrag += dy
                            rightDrag = 0f

                            if (downDrag >= 300f) {
                                downDrag = 0f
                                navController.push(
                                    destination = ControlCenterDestination,
                                    effect = ControlCenterTransitionEffect(compositeOverColor = surfaceColor),
                                    launchMode = LaunchMode.Push(keepPreviousAlive = true)
                                )
                            }
                        }
                        // 向右
                        else if (dx > 0 && kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                            rightDrag += dx
                            downDrag = 0f

                            if (rightDrag >= 300f) {
                                rightDrag = 0f
                                // 右滑返回 TODO 后期做跟手
//                                if(canPop) {
//                                    navController.pop()
//                                } else {
//                                    activity?.finish()
//                                }
                            }
                        }
                    },
                    onDragEnd = {
                        downDrag = 0f
                        rightDrag = 0f
                    },
                    onDragCancel = {
                        downDrag = 0f
                        rightDrag = 0f
                    }
                )
            }
            .combinedClickable(
                onClick = {
                    if(enabled) {
                        navController.pop()
                    } else {
                        activity.finishCurrentActivity()
                    }
                },
                onDoubleClick = null,
                onLongClick = {
                    displayDialog = true
                }
            )
    ) {
        Box(
            modifier = Modifier.padding(DIVIDER_TEXT_VERTICAL_PADDING)
        ) {
            Icon(
                painterResource(
                    if(enabled) {
                        Res.drawable.ic_arrow_back
                    } else {
                        Res.drawable.ic_close
                    }
                ),
                contentDescription = null,
                tint = tint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarNavigationIconForControlCenter(
    tint : Color = MaterialTheme.colorScheme.surface,
    modifier : Modifier = Modifier
) {
    val navController = LocalNavController.current

    IconButton (
        modifier = modifier.padding(start = APP_HORIZONTAL_DP-8.dp),
        onClick = {
            navController.pop()
        },
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor =  MaterialTheme.colorScheme.surface.copy(.6f))
    ) {
        Icon(
            painterResource(Res.drawable.ic_arrow_back),
            null,
            tint = tint,
            modifier = Modifier.size(25.5.dp)
        )
    }
}
