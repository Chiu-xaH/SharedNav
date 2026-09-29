package com.xah.transition.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sharednav.common.kmp.PlatformActivity
import com.sharednav.common.util.LogUtil

actual object PermissionSet {
    @JvmStatic
    actual fun checkAndRequestStoragePermission(activity: PlatformActivity) {
        val actualActivity = activity.activity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = "package:${actualActivity.packageName}".toUri()
                    actualActivity.startActivityForResult(intent, 1)
                } catch (e: Exception) {
                    LogUtil.error(e)
                    // 某些手机拉不出来 , 使用全局设置页面
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    actualActivity.startActivityForResult(intent, 1)
                }
            }

        } else {
            // Android 10 及以下
            val needReq = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).any {
                ContextCompat.checkSelfPermission(actualActivity, it) != PackageManager.PERMISSION_GRANTED
            }

            if (needReq) {
                ActivityCompat.requestPermissions(actualActivity, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1)
            }
        }
    }
}