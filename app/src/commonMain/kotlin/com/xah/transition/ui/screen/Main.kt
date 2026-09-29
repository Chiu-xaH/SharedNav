package com.xah.transition.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.sharednav.common.helper.EnableHelper
import com.sharednav.common.helper.NoneRoundShape
import com.sharednav.common.helper.ScreenCornerHelper
import com.sharednav.common.manager.AnimationSpecManager
import com.xah.container.component.base.SharedContainer
import com.xah.container.component.base.sharedContainer
import com.xah.container.model.ContainerFilledStrategy
import com.xah.container.model.TiltEffect
import com.xah.container.util.LocalSharedRegistry
import com.xah.floating.util.LocalFloatingController
import com.xah.navigation.anim.effect.DefaultTransitionEffect
import com.xah.navigation.anim.effect.IslandTransitionEffect
import com.xah.navigation.anim.effect.JumpTransitionEffect
import com.xah.navigation.anim.effect.PushTransitionEffect
import com.xah.navigation.anim.effect.RollTransitionEffect
import com.xah.navigation.anim.effect.ScaleTransitionEffect
import com.xah.navigation.anim.effect.SlideTransitionEffect2
import com.xah.navigation.anim.effect.SlideTransitionEffect
import com.xah.navigation.anim.effect.rememberDefaultPageEffects
import com.xah.navigation.component.SharedNavHost
import com.xah.navigation.component.rememberNavController
import com.xah.navigation.controller.NavigationController
import com.xah.navigation.model.action.LaunchMode
import com.xah.navigation.model.anim.EffectLevel
import com.xah.navigation.model.anim.effect.sub.Rotation
import com.xah.navigation.model.dest.Destination
import com.xah.navigation.util.LocalNavController
import com.xah.shader.state.shaderSource
import com.xah.shader.style.blurLayer
import com.xah.transition.model.AppIconBean
import com.xah.transition.ui.component.APP_HORIZONTAL_DP
import com.xah.transition.ui.component.CARD_NORMAL_DP
import com.xah.transition.ui.component.CardListItem
import com.xah.transition.ui.component.CustomCard
import com.xah.transition.ui.component.CustomSlider
import com.xah.transition.ui.component.DividerText
import com.xah.transition.ui.component.DividerTextExpandedWithShared
import com.xah.transition.ui.component.PaddingHorizontalDivider
import com.xah.transition.ui.component.TopBarNavigationIcon
import com.xah.transition.ui.component.TopBarNavigationIconForControlCenter
import com.xah.transition.ui.component.TransplantListItem
import com.xah.transition.ui.component.cardNormalColor
import com.xah.transition.ui.screen.nav.destination.AppIconDestination
import com.xah.transition.ui.screen.nav.destination.BezierSettingsDestination
import com.xah.transition.ui.screen.nav.destination.ControlCenterDestination
import com.xah.transition.ui.screen.nav.destination.CornerSettingsDestination
import com.xah.transition.ui.screen.nav.destination.HomeDestination
import com.xah.transition.ui.screen.nav.destination.SecondDestination
import com.xah.transition.ui.screen.nav.destination.ThirdDestination
import com.xah.transition.ui.screen.nav.window.BottomDialogWindow
import com.xah.transition.ui.screen.nav.window.BottomSheetWindow
import com.xah.transition.ui.screen.nav.window.CenterDialogWindow
import com.xah.transition.ui.screen.nav.window.DialogFloatingWindow
import com.xah.transition.ui.screen.test.CubicBezierEditor
import com.xah.transition.ui.style.effect.CONTROL_CENTER_ALPHA
import com.xah.transition.ui.style.effect.ControlCenterTransitionEffect
import com.xah.transition.ui.style.topBarTransplantColor
import com.xah.transition.ui.util.GlobalShaderStateInit
import com.xah.transition.ui.util.LocalPlatformActivity
import com.xah.transition.ui.util.LocalPlatformContext
import com.xah.transition.ui.util.LocalPlatformView
import com.xah.transition.ui.screen.nav.destination.base.NavDestination
import com.xah.transition.ui.holder.GlobalUiStateHolder
import com.xah.transition.ui.screen.nav.destination.FloatingWindowDestination
import com.xah.transition.ui.style.TransitionTheme
import com.xah.transition.util.PermissionSet
import com.xah.transition.util.PlatformView
import com.xah.transition.util.Starter
import com.xah.transition.util.ToastUtil
import com.xah.transition.util.roundOffString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import sharednav.app.generated.resources.Res
import sharednav.app.generated.resources.ic_amap
import sharednav.app.generated.resources.ic_arrow_back
import sharednav.app.generated.resources.ic_candy
import sharednav.app.generated.resources.ic_github
import sharednav.app.generated.resources.ic_home
import sharednav.app.generated.resources.ic_iqiyi
import sharednav.app.generated.resources.ic_jd
import sharednav.app.generated.resources.ic_qweather
import sharednav.app.generated.resources.ic_texture
import sharednav.app.generated.resources.ic_xhs

private fun Modifier.blur(enableBlur : Boolean,radius : Dp) : Modifier {
    return if(enableBlur) {
        this.blur(radius)
    } else {
        this
    }
}

private fun Modifier.scale(
    scale: Float
) : Modifier {
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

private fun Modifier.backgroundEffect(
    enableBlur : Boolean,
    blurRadius : Dp,
    scale: Float
) : Modifier {
    return this
        .blur(enableBlur, blurRadius)
        .scale(scale)
}


@Composable
fun AppThemeMain(
    firstPage : Destination? = null
) = TransitionTheme {
    Main(firstPage)
}

@Composable
private fun Main(
    firstPage : Destination? = null
) {
    val navigationController = rememberNavController(
        firstPage ?: HomeDestination,
    )
    GlobalShaderStateInit()

    val inHomeDest = navigationController.currentDestination == navigationController.startDestination || navigationController.transitionEntry?.to?.destination == navigationController.startDestination || navigationController.transitionEntry?.from?.destination == navigationController.startDestination
    val displayWallpaper = GlobalUiStateHolder.imageBitmap != null
    Box(modifier = Modifier.fillMaxSize()) {
       if(GlobalUiStateHolder.enableWallpaper && displayWallpaper && inHomeDest) {
           val progress = navigationController.transitionProgress.value
           val blurRadius = when(navigationController.transitionLevel) {
               EffectLevel.HIGH -> lerp(navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.blur.start,navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.blur.end,progress)
               else -> navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.blur.start
           }
           val scale = when(navigationController.transitionLevel) {
               EffectLevel.MEDIUM -> lerp(navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.scale.start,2 - navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.scale.end,progress)
               EffectLevel.HIGH -> lerp(navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.scale.start,2 - navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.scale.end,progress)
               EffectLevel.LOW -> navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.scale.start
               EffectLevel.NONE -> navigationController.sharedTransitionEffect.pageEffect.backgroundEffect.effect.scale.start
           }

           Image(
                bitmap = GlobalUiStateHolder.imageBitmap!!,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .backgroundEffect(navigationController.enableBlur, blurRadius, scale),
                contentScale = ContentScale.Crop
            )
       }
        SharedNavHost(
            navController = navigationController,
            modifier = Modifier.let {
                if(GlobalUiStateHolder.enableWallpaper && displayWallpaper) {
                    it
                } else {
                    it.background(MaterialTheme.colorScheme.surface)
                }
            },
        )
    }
}

private val appList = listOf(
    AppIconBean("jd","京东", Res.drawable.ic_jd),
    AppIconBean("xhs","小红书",Res.drawable.ic_xhs),
    AppIconBean("amap","高德地图",Res.drawable.ic_amap),
    AppIconBean("qweather","和风天气",Res.drawable.ic_qweather),
    AppIconBean("iqiyi","爱奇艺",Res.drawable.ic_iqiyi),
    AppIconBean("candy","Candy Crush Saga",Res.drawable.ic_candy),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController = LocalNavController.current
    val scrollState = rememberLazyGridState()
    val registry = LocalSharedRegistry.current
    val context = LocalPlatformContext()
    val floatingController = LocalFloatingController.current
    val imagePicker = rememberImagePicker { bitmap ->
        bitmap?.let {
            GlobalUiStateHolder.imageBitmap = it
        }
    }
    var savedInt by rememberSaveable { mutableStateOf(1) }

    val levelList = remember { EffectLevel.entries }
    val filedList = remember {
        listOf(
            Pair(null,"不强制"),
            Pair(ContainerFilledStrategy.Clip,"裁切"),
            Pair(ContainerFilledStrategy.Stretch,"拉伸"),
            Pair(ContainerFilledStrategy.Color(Color.Black),"色彩"),
        )
    }
    val defaultEffect = DefaultTransitionEffect(rememberDefaultPageEffects())
    val effectList = remember(defaultEffect) {
        listOf(
            Pair(defaultEffect,"默认"),
            Pair(ScaleTransitionEffect(false,true),"缩放"),
            Pair(PushTransitionEffect(),"推入"),
            Pair(SlideTransitionEffect(),"上推"),
            Pair(SlideTransitionEffect2(),"上推2"),
        )
    }
    val activity = LocalPlatformActivity()

    val displayWallpaper = GlobalUiStateHolder.imageBitmap != null
    val d = !GlobalUiStateHolder.enableWallpaper && displayWallpaper

    var showAnimation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showAnimation) {
        showAnimation = true
    }

    var uId by remember { mutableStateOf(888) }
    val shaderState = GlobalUiStateHolder.shaderState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .let {
                shaderState?.let { state ->
                    it.shaderSource(state)
                } ?: it
            }
    ) {
        if(d) {
            Image(
                bitmap = GlobalUiStateHolder.imageBitmap!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Scaffold(
            containerColor = if(!d) MaterialTheme.colorScheme.surface else Color.Transparent,
            floatingActionButton = {
                SharedContainer (
                    containerColor = MaterialTheme.colorScheme.inversePrimary,
                    key = CornerSettingsDestination.key,
                    shape = FloatingActionButtonDefaults.shape as CornerBasedShape
                ) {
                    FloatingActionButton(
                        elevation = FloatingActionButtonDefaults.elevation(0.dp,0.dp,0.dp,0.dp),
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        shape = NoneRoundShape,
                        onClick = {
                            navController.push(CornerSettingsDestination)
                        }
                    ) {
                        Icon(painterResource(CornerSettingsDestination.icon),null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            },
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    state = scrollState,
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(horizontal = APP_HORIZONTAL_DP- CARD_NORMAL_DP*2)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(APP_HORIZONTAL_DP+innerPadding.calculateTopPadding()))
                    }
                    items(appList.size,key = { appList[it].key }) { index ->
                        val item = appList[index]
                        val destination = AppIconDestination(item)
                        Column {
                            SharedContainer(
                                destination.key,
                                containerColor = null,
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clickable {
                                            navController.push(destination)
                                        }
                                ) {
                                    Image(painterResource(item.icon),null)
                                }
                            }
                            Spacer(Modifier.height(APP_HORIZONTAL_DP*2))
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(APP_HORIZONTAL_DP))
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("设置壁纸")
                                },
                                modifier = Modifier.clickable {
                                    imagePicker.launch()
                                }
                            )
                        }
                    }
                    item {
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            val dest = FloatingWindowDestination(1)
                            Card(
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.sharedContainer(dest.key,MaterialTheme.shapes.small),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("FloatingWindowDestination") },
                                    modifier = Modifier.clickable {
                                        navController.push(
                                            dest,
//                                            effect = RollTransitionEffect(clip = false),
                                            launchMode = LaunchMode.Push(keepPreviousAlive = true)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false,true)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("卷起动效(iOS)") },
                                    modifier = Modifier.clickable {
                                        uId = 888
                                        navController.push(
                                            dest,
                                            effect = RollTransitionEffect(clip = false)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(999,false,true)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("卷起动效(透明)") },
                                    modifier = Modifier.clickable {
                                        uId = 999
                                        navController.push(
                                            dest,
                                            effect = RollTransitionEffect(clip = false),
                                            launchMode = LaunchMode.Push(keepPreviousAlive = true)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item {
                        LaunchedEffect(activity) {
                            activity.let { PermissionSet.checkAndRequestStoragePermission(it) }
                        }
                        val surfaceColor = MaterialTheme.colorScheme.surface
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("启动台") },
                                    modifier = Modifier.clickable {
                                        navController.push(
                                            destination = ControlCenterDestination,
                                            effect = ControlCenterTransitionEffect(compositeOverColor = surfaceColor),
                                            launchMode = LaunchMode.Push(keepPreviousAlive = true)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    items(30) { index ->
                        val destination = SecondDestination(userId = index,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer (
                                key = destination.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text("Screen #${index}") },
                                        modifier = Modifier.clickable {
                                            navController.push(destination)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("灵动岛动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = IslandTransitionEffect())
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("对角岛动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = IslandTransitionEffect(position = TransformOrigin(1f,0f), rotation = Rotation(z = 30f)))
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("推入动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = SlideTransitionEffect())
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("推入动效2") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = SlideTransitionEffect2())
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("翻页动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = PushTransitionEffect())
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("从外缩放动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = ScaleTransitionEffect(reservedFgScale = true, reservedBgScale = false))
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("从内缩放动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = ScaleTransitionEffect(reservedFgScale = false, reservedBgScale = true))
                                    }
                                )
                            }
                        }
                    }
                    item {
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("卷起动效") },
                                    modifier = Modifier.clickable {
                                        navController.push(dest, effect = RollTransitionEffect(clip = true))
                                    }
                                )
                            }
                        }
                    }
                    item {
                        LaunchedEffect(activity) {
                            activity.let { PermissionSet.checkAndRequestStoragePermission(it) }
                        }
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("跳转动效1") },
                                    modifier = Modifier.clickable {
                                        navController.push(
                                            dest,
                                            effect = JumpTransitionEffect(context)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item {
                        LaunchedEffect(activity) {
                            activity.let { PermissionSet.checkAndRequestStoragePermission(it) }
                        }
                        val dest = SecondDestination(888,false)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            Card(
                                shape = MaterialTheme.shapes.small,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                TransplantListItem(
                                    headlineContent = { Text("跳转动效2") },
                                    modifier = Modifier.clickable {
                                        navController.push(
                                            dest,
                                            effect = JumpTransitionEffect(alphaStyle = true)
                                        )
                                    }
                                )
                            }
                        }
                    }
                    items(30) { index ->
                        val window = DialogFloatingWindow(index)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer (
                                key = window.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text(window.key) },
                                        modifier = Modifier.clickable {
                                            floatingController.push(window)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val window = BottomDialogWindow
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer (
                                key = window.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text(window.key ?: "底部Dialog") },
                                        modifier = Modifier.clickable {
                                            floatingController.push(window)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val window = BottomSheetWindow
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer (
                                key = window.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text(window.key ?: "底部Sheet") },
                                        modifier = Modifier.clickable {
                                            floatingController.push(window)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val window = CenterDialogWindow
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer (
                                key = window.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text(window.key ?: "中心弹窗") },
                                        modifier = Modifier.clickable {
                                            floatingController.push(window)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        val window = DialogFloatingWindow(888)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer(
                                key = window.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text("容器共享弹窗") },
                                        modifier = Modifier.clickable {
                                            floatingController.push(window)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    items(15,span = { GridItemSpan(maxLineSpan) }) { index ->
                        val destination = SecondDestination(userId = index,true)
                        Box(modifier = Modifier.padding(CARD_NORMAL_DP*2)) {
                            SharedContainer (
                                key = destination.key,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Card(
                                    shape = NoneRoundShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    TransplantListItem(
                                        headlineContent = { Text("Screen #${index}") },
                                        modifier = Modifier.clickable {
                                            navController.push(destination)
                                        }
                                    )
                                }
                            }
                        }
                    }
                    items(levelList.size, key = { levelList[it].levelNum }) { index ->
                        val item = levelList[index]
                        val selected = navController.transitionLevel == item

                        val color = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer

                        Surface(
                            color = color,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("等级 ${item.name}", color = contentColorFor(color))
                                },
                                modifier = Modifier.clickable {
                                    navController.transitionLevel = item
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("容器共享")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(registry.enabled, onCheckedChange = { registry.enabled = it })
                                },
                                modifier = Modifier.clickable {
                                    registry.enabled = !registry.enabled
                                },
                            )
                        }
                    }
                    items(effectList.size, key = { effectList[it].second }) { index ->
                        val item = effectList[index]
                        val selected = navController.defaultTransitionEffect == item.first

                        val color = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer

                        Surface(
                            color = color,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text(item.second, color = contentColorFor(color))
                                },
                                overlineContent = {
                                    Text("非容器共享时的动效", color = contentColorFor(color))
                                },
                                modifier = Modifier.clickable {
                                    navController.defaultTransitionEffect = item.first
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("二次圆角插值")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(checked = registry.enforceQuadraticCornerLerp, onCheckedChange = { registry.enforceQuadraticCornerLerp = it })
                                },
                                modifier = Modifier.clickable {
                                    registry.enforceQuadraticCornerLerp = !registry.enforceQuadraticCornerLerp
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("双向填充")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(registry.extensionDouble, onCheckedChange = { registry.extensionDouble = it })
                                },
                                modifier = Modifier.clickable {
                                    registry.extensionDouble = !registry.extensionDouble
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LaunchedEffect(GlobalUiStateHolder.enablePredictiveBack) {
                            navController.enablePredictiveBack = GlobalUiStateHolder.enablePredictiveBack
                            registry.enablePredictiveBack = GlobalUiStateHolder.enablePredictiveBack
                        }
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("预测式返回")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(GlobalUiStateHolder.enablePredictiveBack, onCheckedChange = { GlobalUiStateHolder.enablePredictiveBack = it })
                                },
                                modifier = Modifier.clickable {
                                    GlobalUiStateHolder.enablePredictiveBack = !GlobalUiStateHolder.enablePredictiveBack
                                },
                            )
                        }
                    }
                    /*
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("预测式返回背景固定")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(!navController.enablePredictiveBackBackgroundFollow, onCheckedChange = { navController.enablePredictiveBackBackgroundFollow = !navController.enablePredictiveBackBackgroundFollow })
                                },
                                modifier = Modifier.clickable {
                                    navController.enablePredictiveBackBackgroundFollow = !navController.enablePredictiveBackBackgroundFollow
                                },
                            )
                        }
                    }
                     */
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LaunchedEffect(GlobalUiStateHolder.enablePredictiveBack) {
                            navController.enablePredictiveBack = GlobalUiStateHolder.enablePredictiveBack
                            registry.enablePredictiveBack = GlobalUiStateHolder.enablePredictiveBack
                        }
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("Splash Screen")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(navController.enableSplashScreen, onCheckedChange = { navController.enableSplashScreen = it })
                                },
                                modifier = Modifier.clickable {
                                    navController.enableSplashScreen = !navController.enableSplashScreen
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LaunchedEffect(GlobalUiStateHolder.enablePredictiveBack) {
                            navController.enablePredictiveBack = GlobalUiStateHolder.enablePredictiveBack
                            registry.enablePredictiveBack = GlobalUiStateHolder.enablePredictiveBack
                        }
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("壁纸缩放")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_texture),null)
                                },
                                trailingContent = {
                                    Switch(enabled = displayWallpaper, checked = GlobalUiStateHolder.enableWallpaper, onCheckedChange = { GlobalUiStateHolder.enableWallpaper = it })
                                },
                                modifier = Modifier.clickable(displayWallpaper) {
                                    GlobalUiStateHolder.enableWallpaper = !GlobalUiStateHolder.enableWallpaper
                                },
                            )
                        }
                    }
                    items(filedList.size, key = { filedList[it].hashCode() }) { index ->
                        val item = filedList[index]
                        val selected = registry.enforceContainerFilledStrategy == item.first

                        val color = if(selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer

                        Surface(
                            color = color,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text(item.second, color = contentColorFor(color))
                                },
                                overlineContent = {
                                    Text("容器填充方案",color = contentColorFor(color))
                                },
                                modifier = Modifier.clickable {
                                    registry.enforceContainerFilledStrategy = item.first
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            Column {
                                TransplantListItem(
                                    headlineContent = {
                                        Text("动画速率倍数 x${AnimationSpecManager.speedRadio}")
                                    },
                                )
                                CustomSlider(
                                    value = AnimationSpecManager.speedRadio,
                                    onValueChange = {
                                        AnimationSpecManager.speedRadio = it
                                    },
                                    modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                    valueRange = 0.5f..5f,
                                    steps = 90,
                                    showProcessText = true,
                                    processText = AnimationSpecManager.speedRadio.toString()
                                )
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            Column {
                                var valueQuadraticBezierRectInterpolatorHorizontalRadio by remember { mutableFloatStateOf(registry.quadraticBezierRectInterpolatorHorizontalRadio) }
                                TransplantListItem(
                                    headlineContent = {
                                        Text("路径曲线X轴向心系数 $valueQuadraticBezierRectInterpolatorHorizontalRadio")
                                    },
                                )
                                CustomSlider(
                                    value = valueQuadraticBezierRectInterpolatorHorizontalRadio,
                                    onValueChange = {
                                        valueQuadraticBezierRectInterpolatorHorizontalRadio = it
                                        registry.quadraticBezierRectInterpolatorHorizontalRadio = valueQuadraticBezierRectInterpolatorHorizontalRadio
                                    },
                                    modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                    valueRange = 0f..10f,
                                    showProcessText = true,
                                    processText = valueQuadraticBezierRectInterpolatorHorizontalRadio.toString()
                                )

                                var valueQuadraticBezierRectInterpolatorVerticalRadio by remember { mutableFloatStateOf(registry.quadraticBezierRectInterpolatorVerticalRadio) }

                                PaddingHorizontalDivider()
                                TransplantListItem(
                                    headlineContent = {
                                        Text("路径曲线Y轴向心系数 $valueQuadraticBezierRectInterpolatorVerticalRadio")
                                    },
                                )
                                CustomSlider(
                                    value = valueQuadraticBezierRectInterpolatorVerticalRadio,
                                    onValueChange = {
                                        valueQuadraticBezierRectInterpolatorVerticalRadio = it
                                        registry.quadraticBezierRectInterpolatorVerticalRadio = valueQuadraticBezierRectInterpolatorVerticalRadio
                                    },
                                    modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                    valueRange = 0f..10f,
                                    showProcessText = true,
                                    processText = valueQuadraticBezierRectInterpolatorVerticalRadio.toString()
                                )
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            Column {
                                var valueSpeedUpRadioAlpha by remember { mutableFloatStateOf(registry.speedUpRadioAlpha) }
                                TransplantListItem(
                                    headlineContent = {
                                        Text("容器共享透明度速率比 $valueSpeedUpRadioAlpha")
                                    },
                                )
                                CustomSlider(
                                    value = valueSpeedUpRadioAlpha,
                                    onValueChange = {
                                        valueSpeedUpRadioAlpha = it
                                        registry.speedUpRadioAlpha = valueSpeedUpRadioAlpha
                                    },
                                    modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                    valueRange = 1f..10f,
                                    steps = 35,
                                    showProcessText = true,
                                    processText = valueSpeedUpRadioAlpha.toString()
                                )

                                var valueSpeedUpRadioCorner by remember { mutableFloatStateOf(registry.speedUpRadioCorner) }

                                PaddingHorizontalDivider()
                                TransplantListItem(
                                    headlineContent = {
                                        Text("容器共享圆角速率比 $valueSpeedUpRadioCorner")
                                    },
                                )
                                CustomSlider(
                                    value = valueSpeedUpRadioCorner,
                                    onValueChange = {
                                        valueSpeedUpRadioCorner = it
                                        registry.speedUpRadioCorner = valueSpeedUpRadioCorner
                                    },
                                    modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                    valueRange = 1f..10f,
                                    steps = 35,
                                    showProcessText = true,
                                    processText = valueSpeedUpRadioCorner.toString()
                                )
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            Column {
                                TransplantListItem(
                                    headlineContent = {
                                        Text("容器形变效果")
                                    },
                                    leadingContent = {
                                        Icon(painterResource(Res.drawable.ic_texture),null)
                                    },
                                )
                                Row {
                                    TiltEffect.entries.forEach { item ->
                                        val bgColor = if(registry.tiltEffect == item) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            cardNormalColor()
                                        }
                                        TransplantListItem(
                                            headlineContent = {
                                                Text(
                                                    when(item) {
                                                        TiltEffect.NONE -> "无"
                                                        TiltEffect.SHADER_2 -> "拉伸(单)"
                                                        TiltEffect.SHADER_4 -> "拉伸(双)"
                                                        TiltEffect.ROTATION -> "倾斜"
                                                    }
                                                    , color = contentColorFor(bgColor)
                                                )
                                            },
                                            overlineContent =
                                                if(!EnableHelper.canShader) {
                                                    { Text("Android 13+", color = contentColorFor(bgColor)) }
                                                } else {
                                                    { Text("正在开发", color = contentColorFor(bgColor)) }
                                                },
                                            modifier = Modifier
                                                .weight(1/3f)
                                                .clickable {
                                                    if(!EnableHelper.canShader) {
                                                        return@clickable
                                                    }
                                                    registry.tiltEffect = item
                                                },
                                            colors = bgColor
                                        )
                                    }
                                }
                                if(registry.tiltEffect != TiltEffect.NONE) {
                                    var valueTiltMaxValue by remember { mutableFloatStateOf(registry.tiltMaxValue) }
                                    val valueTiltMaxValueText = remember(valueTiltMaxValue) { valueTiltMaxValue.roundOffString(2) }

                                    PaddingHorizontalDivider()
                                    TransplantListItem(
                                        headlineContent = {
                                            Text("容器共享倾斜程度 $valueTiltMaxValueText")
                                        },
                                    )
                                    CustomSlider(
                                        value = valueTiltMaxValue,
                                        onValueChange = {
                                            valueTiltMaxValue = it
                                            registry.tiltMaxValue = valueTiltMaxValue
                                        },
                                        modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                        valueRange = 0f..50f,
                                        steps = 39,
                                        showProcessText = true,
                                        processText = valueTiltMaxValueText
                                    )

                                    var valueSpeedUpRadioTilt by remember { mutableFloatStateOf(registry.speedUpRadioTilt) }
                                    val valueSpeedUpRadioTiltText = remember(valueSpeedUpRadioTilt) { valueSpeedUpRadioTilt.roundOffString(2) }

                                    PaddingHorizontalDivider()
                                    TransplantListItem(
                                        headlineContent = {
                                            Text("容器共享倾斜速率比 $valueSpeedUpRadioTiltText")
                                        },
                                    )
                                    CustomSlider(
                                        value = valueSpeedUpRadioTilt,
                                        onValueChange = {
                                            valueSpeedUpRadioTilt = it
                                            registry.speedUpRadioTilt = valueSpeedUpRadioTilt
                                        },
                                        modifier = Modifier.padding(bottom = APP_HORIZONTAL_DP),
                                        valueRange = 1f..10f,
                                        steps = 35,
                                        showProcessText = true,
                                        processText = valueSpeedUpRadioTiltText
                                    )
                                }
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Surface(
                            color = cardNormalColor(),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(CARD_NORMAL_DP*2)
                        ) {
                            TransplantListItem(
                                headlineContent = {
                                    Text("Github")
                                },
                                supportingContent = {
                                    Text("引入本库在您的Android、KMP-iOS、KMP-Desktop、KMP-Web(待适配)项目上方便地实现此效果")
                                },
                                leadingContent = {
                                    Icon(painterResource(Res.drawable.ic_github),null)
                                },
                                modifier = Modifier.clickable {
                                    Starter.startWebUrl(context,"https://github.com/Chiu-xaH/SharedNav")
                                },
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier
                            .navigationBarsPadding()
                            .height((APP_HORIZONTAL_DP + innerPadding.calculateBottomPadding()) * 3))
                    }
//                    item(span = { GridItemSpan(maxLineSpan) }) {
//                        CardListItem(
//                            headlineContent = { Text("保存数据 ${savedInt}")},
//                            modifier = Modifier.clickable {
//                                savedInt++
//                            }
//                        )
//                    }
                }
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides 0.dp
                ) {
                    SharedContainer(
                        containerColor = MaterialTheme.colorScheme.inversePrimary,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(horizontal = APP_HORIZONTAL_DP)
                        ,
                        key = BezierSettingsDestination.key,
                        shape = CircleShape
                    ) {
                        FilledTonalIconButton (
                            shape = NoneRoundShape,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.inversePrimary),
                            onClick = {
                                navController.push(BezierSettingsDestination)
                            }
                        ) {
                            Icon(painterResource(BezierSettingsDestination.icon),null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(
    userId : Int,
    useShader : Boolean = false
) {
    val navController = LocalNavController.current
    val shaderState = GlobalUiStateHolder.shaderState
    val useShaderFinal = shaderState != null && useShader && (
            (navController.transitionEntry?.from?.destination is HomeDestination && navController.transitionEntry?.to?.destination is SecondDestination) ||
                    (navController.transitionEntry?.from?.destination is SecondDestination && navController.transitionEntry?.to?.destination is HomeDestination)
            )

    if(useShaderFinal) {
        val progress = when (userId) {
            999 -> 1f - navController.transitionProgress.value
            777 -> navController.transitionProgress.value
            else -> 1f
        }
        val maxValue = when (userId) {
            999 -> 25.dp
            777 -> 25.dp
            else -> 20.dp
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blurLayer(
                    shaderState,
                    if(navController.transitionLevel == EffectLevel.HIGH) {
                        (progress*(maxValue.value)).dp
                    } else {
                        0.dp
                    }
                )
        )
    }
    if(userId == 999) {
//        ControlCenterScreen()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                MediumTopAppBar(
                    colors = topBarTransplantColor(),
                    title = { Text("") },
                    navigationIcon = {
                        TopBarNavigationIconForControlCenter()
                    },
                )
            },
        ) {}
        return
    }
    Scaffold(
        modifier = Modifier
            .let {
                if(useShaderFinal) {
                    it.alpha(navController.transitionProgress.value)
                } else {
                    it
                }
            }
        ,
        topBar = {
            MediumTopAppBar(
                colors = topBarTransplantColor(),
                title = { Text("二级界面") },
                navigationIcon = {
                    TopBarNavigationIcon()
                }
            )
        },
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn {
                item { Spacer(Modifier.height(innerPadding.calculateTopPadding())) }
                items(20) {
                    DividerTextExpandedWithShared("副标题$it") {
                        CustomCard(
                            color = cardNormalColor(),
                            modifier = Modifier.clickable {
                                navController.push(ThirdDestination)
                            }
//                                .sharedContainer(
//                                    key = ThirdDestination.key,
//                                    shape = MaterialTheme.shapes.medium
//                                )
                        ) {
                            repeat(3) { r ->
                                TransplantListItem(
                                    headlineContent = {
                                        Text("Push to Third $r")
                                    },
                                )
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(innerPadding.calculateBottomPadding())) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThirdScreen() {
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            MediumTopAppBar(
                colors = topBarTransplantColor(),
                title = { Text("Third") },
                navigationIcon = {
                    TopBarNavigationIcon()
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Button(
                onClick = {
                    navController.backToHome()
                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("To Home")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CornerSettingsScreen(title : String) {
    val navController = LocalNavController.current
    val view = LocalPlatformView()

    var corner by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        corner = ScreenCornerHelper.corner.value
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.inversePrimary,
        topBar = {
            MediumTopAppBar(
                colors = topBarTransplantColor(),
                title = { Text(title) },
                navigationIcon = {
                    TopBarNavigationIcon()
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    ScreenCornerHelper.corner = corner.dp
                    navController.pop()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(APP_HORIZONTAL_DP)
                    .navigationBarsPadding()
            ) {
                Text("保存")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(corner.dp))
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column (modifier = Modifier.align(Alignment.Center),horizontalAlignment = Alignment.CenterHorizontally) {
                CustomSlider(
                    value = corner,
                    onValueChange = {
                        corner = it
                    },
                    valueRange = 0f..100f
                )
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = APP_HORIZONTAL_DP)) {
                    FilledTonalButton(
                        onClick = {
                            corner -= 0.5f
                        },
                        enabled = corner > 0f,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text("-0.5")
                    }
                    FilledTonalButton(
                        onClick = {
                            corner = getDefaultScreenCorner(view)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text("$corner")
                    }
                    FilledTonalButton(
                        onClick = {
                            corner += 0.5f
                        },
                        enabled = corner < 100f,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text("+0.5")
                    }
                }
            }
        }
    }
}

expect fun getDefaultScreenCorner(view : PlatformView): Float

@Composable
expect fun rememberImagePicker(onResult: (ImageBitmap?) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BezierSettingsScreen(title: String) {
    val registry = LocalSharedRegistry.current
    var isPush by rememberSaveable { mutableStateOf(true) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                colors = topBarTransplantColor(),
                title = { Text(title) },
                navigationIcon = {
                    TopBarNavigationIcon()
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    isPush = !isPush
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(APP_HORIZONTAL_DP)
                    .navigationBarsPadding()
            ) {
                Text("当前调节${
                    if(isPush) {
                        "PUSH"
                    } else {
                        "POP"
                    }
                }")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            if(isPush) {
                CubicBezierEditor(
                    registry.pushX1,
                    registry.pushY1,
                    registry.pushX2,
                    registry.pushY2,
                    { registry.pushX1 = it },
                    { registry.pushY1 = it },
                    { registry.pushX2 = it },
                    { registry.pushY2 = it },
                )
            } else {
                CubicBezierEditor(
                    registry.popX1,
                    registry.popY1,
                    registry.popX2,
                    registry.popY2,
                    { registry.popX1 = it },
                    { registry.popY1 = it },
                    { registry.popX2 = it },
                    { registry.popY2 = it },
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlCenterScreen() {
    val navController = LocalNavController.current
    val stack = navController.stack.reversed().drop(1)
    val contentColor = MaterialTheme.colorScheme.onSurface
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val threshold = remember { 120f }
    val listState = rememberLazyListState()
    var overscroll by remember { mutableFloatStateOf(0f) }
    val listNestedScrollConnection = remember(navController) {
        object : NestedScrollConnection {

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val layoutInfo = listState.layoutInfo
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()

                val atBottom =
                    lastVisible != null &&
                            lastVisible.index == layoutInfo.totalItemsCount - 1 &&
                            lastVisible.offset + lastVisible.size <= layoutInfo.viewportEndOffset

                if (atBottom && available.y < 0) {
                    overscroll += -available.y
                    if (overscroll >= threshold) {
                        overscroll = 0f
                        navController.pop()
                    }
                } else {
                    overscroll = 0f
                }

                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            MediumTopAppBar(
                scrollBehavior = scrollBehavior,
                colors = topBarTransplantColor(),
                title = {
                    Text(
                        "启动台",
                        color = contentColor,
                        style =  LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 20f
                            )
                        ),
                    )
                },
                navigationIcon = {
                    TopBarNavigationIconForControlCenter(contentColor)
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if(navController.previousDestination() != navController.startDestination){
                            IconButton (
                                onClick = {
                                    GlobalScope.launch {
                                        navController.pop()
                                        delay(50)
                                        navController.awaitTransition()
                                        navController.backToHome()
                                        ToastUtil.showToast("已回到首页")
                                    }
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor =  MaterialTheme.colorScheme.errorContainer.copy(.75f))
                            ) {
                                Icon(
                                    painterResource(Res.drawable.ic_home),
                                    null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(25.5.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(APP_HORIZONTAL_DP-8.dp))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        navController.pop()
                    }
            )
            LazyColumn(
//                state = listState,
//                modifier = Modifier.nestedScroll(listNestedScrollConnection)
            ) {
                item { Spacer(Modifier.height(innerPadding.calculateTopPadding())) }
                item {
                    DividerText(
                        "栈内页面",
                        contentColor = contentColor,
                        style =  LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 20f
                            )
                        ),
                    )
                }
                items(stack.size,key = { stack[it].id }) { index ->
                    val item = stack[index]
                    val dest = item.destination as NavDestination
                    val title = dest.title
                    val isCurrent = index == 0

                    CardListItem(
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
                                navController.pop()
                            } else {
                                GlobalScope.launch {
                                    navController.pop()
                                    delay(50)
                                    navController.awaitTransition()
                                    navController.push(
                                        item.destination,
                                        LaunchMode.PopToExisting()
                                    )
                                }
                            }
                        },
                        color = MaterialTheme.colorScheme.surface.copy(1-CONTROL_CENTER_ALPHA)
                    )
                }
                item {
                    DividerText(
                        "历史记录",
                        contentColor = contentColor,
                        style =  LocalTextStyle.current.copy(
                            shadow = Shadow(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                offset = Offset(0f, 0f),
                                blurRadius = 20f
                            )
                        ),
                    )
                }
                item {
                    CardListItem(
                        headlineContent = { Text("正在开发") },
                        leadingContent = {
                            Icon(painterResource(Res.drawable.ic_texture),null)
                        },
                        modifier = Modifier.clickable {

                        },
                        color = MaterialTheme.colorScheme.surface.copy(1-CONTROL_CENTER_ALPHA)
                    )
                }
                /*
                item {
                    DividerTextExpandedWithShared("快速打开",contentColor = contentColor) {
                        CardListItem(
                            headlineContent = { Text("正在开发") },
                            leadingContent = {
                                Icon(painterResource(Res.drawable.ic_texture),null)
                            },
                            modifier = Modifier.clickable {

                            },
                            color = MaterialTheme.colorScheme.surface.copy(1-CONTROL_CENTER_ALPHA)
                        )
                    }
                }
                 */
                item { Spacer(Modifier.height(innerPadding.calculateBottomPadding())) }
            }
        }
    }
}

private fun NavigationController.backToHome(
    reuse : Boolean = true
) {
    if(reuse) {
        if(containsDestination(startDestination)) {
            push(
                startDestination,
                LaunchMode.PopToExisting()
            )
        } else {
            push(
                startDestination,
                LaunchMode.Clear()
            )
        }
    } else {
        push(
            startDestination,
            LaunchMode.Clear(reuse = false)
        )
    }
}

