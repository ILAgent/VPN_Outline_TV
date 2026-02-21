package com.ilagent.nativeoutline.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager

@Composable
fun WhiteList(preferencesManager: PreferencesManager, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var isAppSelectionDialogOpen by remember { mutableStateOf(false) }
    val selectedApps = remember { mutableStateListOf<String>() }
    val isWhitelistMode = remember {
        derivedStateOf { !selectedApps.contains("all_apps") }
    }

    LaunchedEffect(Unit) {
        val savedApps = preferencesManager.getSelectedApps()
        if (savedApps.isNullOrEmpty()) {
            selectedApps.clear()
            selectedApps.add("all_apps")
            preferencesManager.saveSelectedApps(selectedApps.toList())
        } else {
            selectedApps.clear()
            selectedApps.addAll(savedApps)
        }
    }
    Column(modifier.selectableGroup()) {
        Column() {
            // Выбор режима: для всех или белый список
            SettingsDialogThemeChooserRow(
                text = stringResource(id = R.string.for_all_apps),
                selected = !isWhitelistMode.value,
                onClick = {
                    selectedApps.add("all_apps")
                    preferencesManager.saveSelectedApps(selectedApps.toList())
                }
            )

            SettingsDialogThemeChooserRow(
                text = stringResource(id = R.string.whitelist_mode),
                selected = isWhitelistMode.value,
                onClick = {
                    selectedApps.remove("all_apps")
                    preferencesManager.saveSelectedApps(selectedApps.toList())
                }
            )
        }
        if (isWhitelistMode.value) {
            Spacer(modifier = Modifier.height(8.dp))

            // Кнопка "Добавить"
            Button(
                onClick = { isAppSelectionDialogOpen = true },
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    text = stringResource(id = R.string.add_an_application)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Подсказка, когда белый список пуст
            if (selectedApps.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.whitelist_empty_hint),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }

            // Список приложений в белом списке
            LazyColumn {
                selectedApps.filter { it != "all_apps" }.forEach { packageName ->
                    val (appName, appIcon) = try {
                        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                        packageManager.getApplicationLabel(applicationInfo).toString() to
                                packageManager.getApplicationIcon(applicationInfo)
                    } catch (_: Exception) {
                        // todo log e
                        packageName to null
                    }
                    item {
                        WhiteListItem(appName, appIcon) {
                            selectedApps.remove(packageName)
                            preferencesManager.saveSelectedApps(selectedApps.toList())
                        }
                    }
                }
            }
        }
    }

    if (isAppSelectionDialogOpen) {
        AppSelectionDialog(
            onDismiss = { isAppSelectionDialogOpen = false },
            initialSelectedApps = selectedApps.filter { it != "all_apps" }.toList(),
            onAppsSelected = { apps ->
                selectedApps.clear()
                selectedApps.addAll(apps)
                preferencesManager.saveSelectedApps(selectedApps.toList())
            }
        )
    }
}

@Composable
private fun WhiteListItem(appName: String, icon: Drawable?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.toBitmap()?.asImageBitmap()?.let { icon ->
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(all = 4.dp)
            )
        }
        Text(
            text = appName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.remove),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview
@Composable
fun WhiteListItemPreview() {
    WhiteListItem("Youtube", null) { }
}

@Preview
@Composable
fun WhiteListPreview() {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)
    WhiteList(preferencesManager)
}