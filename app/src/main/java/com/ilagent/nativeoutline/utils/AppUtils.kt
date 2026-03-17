package com.ilagent.nativeoutline.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode

fun versionName(context: Context): String {
    return try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "unknown"
    } catch (e: Exception) {
        CrashlyticsLogger.logException(e, "Failed to get version name")
        "unknown"
    }
}

@Composable
fun versionNameUi(context: Context): String {
    if (LocalInspectionMode.current) return "0.0"
    return versionName(context)
}