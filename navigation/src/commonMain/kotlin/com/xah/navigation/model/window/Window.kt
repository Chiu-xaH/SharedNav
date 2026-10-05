package com.xah.navigation.model.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xah.floating.anim.DefaultForegroundEffect
import com.xah.floating.model.anim.ForegroundEffect

abstract class Window {
    /**
     * 也作为共享容器Key
     */
    abstract val key : String

    @Composable
    abstract fun BoxScope.Content()

    @Composable
    open fun Layer() {
        Box(modifier = Modifier.fillMaxSize()) {
            Content()
        }
    }
}