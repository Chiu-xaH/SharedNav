package com.xah.transition.util

import com.sharednav.common.kmp.PlatformActivity

expect object PermissionSet {
    fun checkAndRequestStoragePermission(activity: PlatformActivity)
}