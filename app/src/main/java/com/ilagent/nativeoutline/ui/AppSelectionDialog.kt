package com.ilagent.nativeoutline.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.model.AppInfo
import com.ilagent.nativeoutline.domain.installedapps.installedApps

@Composable
fun AppSelectionDialog(
    onDismiss: () -> Unit,
    initialSelectedApps: List<String>,
    onAppsSelected: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val appList = remember { mutableStateListOf<AppInfo>() }
    val selectedApps = remember { mutableStateListOf<String>() }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        selectedApps.clear()
        selectedApps.addAll(initialSelectedApps.filter { it != "all_apps" })

        appList.clear()
        val apps = installedApps(requireContext = { context }, selectedApps = selectedApps)
        appList.addAll(apps)
        isLoading = false
    }


    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.select_applications),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(id = R.string.search_apps)) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                val filteredAppList = appList.filter { appInfo ->
                    appInfo.appName.contains(searchQuery, ignoreCase = true)
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredAppList) { appInfo ->
                            AppListItem(appInfo, onAppSelected = { selectedApp, isSelected ->
                                val index = appList.indexOf(selectedApp)
                                if (index >= 0) {
                                    appList[index] = selectedApp.copy(isSelected = isSelected)
                                    if (isSelected) {
                                        selectedApps.add(selectedApp.packageName)
                                    } else {
                                        selectedApps.remove(selectedApp.packageName)
                                    }
                                }
                            })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        onAppsSelected(selectedApps.toList())
                        onDismiss()
                    }) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(appInfo: AppInfo, onAppSelected: (AppInfo, Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onAppSelected(appInfo, !appInfo.isSelected)
            }
            .padding(8.dp)
    ) {
        val appIconBitmap = appInfo.icon.toBitmap()
        Image(
            bitmap = appIconBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = appInfo.appName, modifier = Modifier.weight(1f))
        Checkbox(
            checked = appInfo.isSelected,
            onCheckedChange = {
                onAppSelected(appInfo, it)
            }
        )
    }
}
