package com.xah.transition.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.sharednav.common.kmp.PlatformView
import com.xah.transition.util.ToastUtil

actual fun getDefaultScreenCorner(view: PlatformView): Float = 0f

@Composable
actual fun rememberImagePicker(onResult: (ImageBitmap?) -> Unit): ImagePickerLauncher {
    return object : ImagePickerLauncher {
        override fun launch() {
            ToastUtil.showToast("正在开发")
            onResult(null)
        }
    }
}