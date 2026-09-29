package com.xah.transition.ui.screen

import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.sharednav.common.helper.ScreenCornerHelper
import com.sharednav.common.kmp.PlatformView

actual fun getDefaultScreenCorner(view : PlatformView): Float = ScreenCornerHelper(view.view).getCornerDp().value

@Composable
actual fun rememberImagePicker(onResult: (ImageBitmap?) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { imageUri ->
            // 使用 context 读取图片为 ImageBitmap
            onResult(
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri).asImageBitmap()
            )
        }
    }
    return object : ImagePickerLauncher {
        override fun launch() {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}