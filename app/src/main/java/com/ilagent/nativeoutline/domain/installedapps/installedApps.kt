package com.ilagent.nativeoutline.domain.installedapps

import android.content.Context
import com.ilagent.nativeoutline.data.model.AppInfo
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun CoroutineScope.installedApps(
    requireContext: () -> Context,
    selectedApps: List<String>,
): List<AppInfo> {
    return withContext(Dispatchers.IO) {
        val pm = requireContext().packageManager
        pm.getInstalledApplications(0)
            .filter { it.packageName != requireContext().packageName }
            .mapNotNull {
                try {
                    AppInfo(
                        appName = pm.getApplicationLabel(it).toString(),
                        packageName = it.packageName,
                        icon = pm.getApplicationIcon(it.packageName),
                        isSelected = selectedApps.contains(it.packageName)
                    )
                } catch (e: Exception) {
                    // Skip apps that can't be loaded (e.g., NameNotFoundException)
                    CrashlyticsLogger.logException(e)
                    null
                }
            }
            .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
    }
}