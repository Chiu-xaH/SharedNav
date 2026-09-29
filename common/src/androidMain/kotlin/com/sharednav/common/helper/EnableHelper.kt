package com.sharednav.common.helper

import android.os.Build

actual object EnableHelper {
    actual val canShader: Boolean = Build.VERSION.SDK_INT >= 33
    actual val canBlur: Boolean = Build.VERSION.SDK_INT >= 31
    actual val canPredictedGesture: Boolean = Build.VERSION.SDK_INT >= 34
}