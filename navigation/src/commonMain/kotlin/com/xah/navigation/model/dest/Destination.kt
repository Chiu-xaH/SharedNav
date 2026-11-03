package com.xah.navigation.model.dest

import androidx.compose.runtime.Composable

abstract class Destination {
    /**
     * 也作为共享容器Key
     */
    abstract val key: String

    /**
     * 突破Controller的enableSplashScreen限制，自主决定是否使用PlaceHolder，为空时跟随全局
     */
    open val enforcePlaceHolder : Boolean? = null

    /**
     * 是否等PUSH动画完成后再加载，如果是则需要传一个PlaceHolder(Splash Screen)先显示，比如初始化相机时如果不延迟加载动效就会卡顿
     * POP时不显示PlaceHolder，POP瞬间抓取一张冻结的ImageBitmap显示并返回，最大限度地降低动效卡顿
     */
    open val PlaceHolder: (@Composable () -> Unit)? = null

    /**
     * 是否允许横屏旋转，null则交给上层判断
     */
    open val requestedOrientation : Int? = null

    // ...可扩展

    @Composable
    abstract fun Content()
}

