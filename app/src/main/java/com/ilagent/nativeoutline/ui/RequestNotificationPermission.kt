package com.ilagent.nativeoutline.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun NotificationPermission(request: Boolean, onResult: (Boolean) -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        if (request) onResult(true)
        return
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
    if (!request) return
    val permission = Manifest.permission.POST_NOTIFICATIONS
    val hasPermission = ContextCompat.checkSelfPermission(LocalContext.current, permission) ==
            PackageManager.PERMISSION_GRANTED

    if (hasPermission) {
        onResult(true)
    } else {
        requestPermissionLauncher.launch(permission)
    }
}