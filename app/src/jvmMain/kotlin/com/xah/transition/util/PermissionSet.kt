package com.xah.transition.util

import com.sharednav.common.kmp.PlatformActivity

actual object PermissionSet {
    @JvmStatic
    actual fun checkAndRequestStoragePermission(activity: PlatformActivity) = Unit
}