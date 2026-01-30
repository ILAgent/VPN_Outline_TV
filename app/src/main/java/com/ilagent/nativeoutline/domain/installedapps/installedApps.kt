package com.ilagent.nativeoutline.domain.installedapps

import android.content.Context
import com.ilagent.nativeoutline.data.model.AppInfo
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
            .map {
                AppInfo(
                    appName = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                    icon = pm.getApplicationIcon(it.packageName),
                    isSelected = selectedApps.contains(it.packageName)
                )
            }
            .sortedWith(compareBy({ !it.isSelected }, { it.appName.lowercase() }))
    }
}