package com.ilagent.nativeoutline.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemSecurityUpdateWarning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import com.ilagent.nativeoutline.viewmodel.AutoConnectViewModel
import com.ilagent.nativeoutline.viewmodel.DnsViewModel
import com.ilagent.nativeoutline.viewmodel.LanguageViewModel
import com.ilagent.nativeoutline.viewmodel.ThemeMode
import com.ilagent.nativeoutline.viewmodel.ThemeViewModel

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    preferencesManager: PreferencesManager,
    onDnsSelected: (String) -> Unit,
    themeViewModel: ThemeViewModel,
    autoConnectViewModel: AutoConnectViewModel,
    languageViewModel: LanguageViewModel,
    dnsViewModel: DnsViewModel
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDnsDialog by remember { mutableStateOf(false) }

    val isAutoConnectionEnabled by autoConnectViewModel.isAutoConnectEnabled.collectAsState()
    val selectedThemeMode by themeViewModel.themeMode.collectAsState()
    val selectedLanguage by languageViewModel.languageCode
    val selectedDns by dnsViewModel.dnsCode

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SettingsDialogSectionTitle(text = stringResource(id = R.string.DNS))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDnsDialog = true }
                        .padding(12.dp)
                ) {
                    Text(
                        text = getDnsDisplayName(selectedDns),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsDialogSectionTitle(text = stringResource(id = R.string.theme))
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_moon),
                    contentDescription = "Theme",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                )
                
                SettingsDialogRadioItem(
                    text = stringResource(id = R.string.theme_light),
                    selected = selectedThemeMode == ThemeMode.LIGHT,
                    onClick = {
                        themeViewModel.setThemeMode(ThemeMode.LIGHT)
                        CrashlyticsLogger.logThemeChanged("light")
                    }
                )
                
                SettingsDialogRadioItem(
                    text = stringResource(id = R.string.theme_dark),
                    selected = selectedThemeMode == ThemeMode.DARK,
                    onClick = {
                        themeViewModel.setThemeMode(ThemeMode.DARK)
                        CrashlyticsLogger.logThemeChanged("dark")
                    }
                )
                
                SettingsDialogRadioItem(
                    text = stringResource(id = R.string.theme_system),
                    selected = selectedThemeMode == ThemeMode.SYSTEM,
                    onClick = {
                        themeViewModel.setThemeMode(ThemeMode.SYSTEM)
                        CrashlyticsLogger.logThemeChanged("system")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SettingsDialogSectionTitle(text = stringResource(id = R.string.language))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(12.dp)
                ) {
                    Text(
                        text = getLanguageDisplayName(selectedLanguage, LocalContext.current),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsDialogSectionTitle(text = stringResource(id = R.string.system))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SystemSecurityUpdateWarning,
                        contentDescription = "Auto connection",
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(id = R.string.auto_connection),
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = isAutoConnectionEnabled,
                        onCheckedChange = { isChecked ->
                            autoConnectViewModel.setAutoConnectEnabled(isChecked)
                            CrashlyticsLogger.logAutoConnectionChanged(isChecked)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                LinksPanel()
            }
        },
        confirmButton = {
            Text(
                text = stringResource(id = R.string.save),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        onDismiss()
                    },
            )
        }
    )

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            languageViewModel = languageViewModel,
            onLanguageSelected = { languageCode ->
                languageViewModel.setLanguage(languageCode)
                CrashlyticsLogger.logLanguageChanged(languageCode)
            }
        )
    }

    if (showDnsDialog) {
        DnsDialog(
            onDismiss = { showDnsDialog = false },
            dnsViewModel = dnsViewModel,
            onDnsSelected = { dnsCode ->
                dnsViewModel.setDns(dnsCode)
                onDnsSelected(dnsCode)
                CrashlyticsLogger.logDnsChanged(dnsCode)
            }
        )
    }
}


@Composable
fun LinksPanel() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.qr),
                        contentDescription = "Telegram QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.go_to_group),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    // todo
                                    "https://t.me/vpntv_group".toUri()
                                )
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                CrashlyticsLogger.logException(e, "Failed to open Telegram link")
                            }
                        }
                    )
                }
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        // todo
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier
//                .clickable { showDialog = true }
//                .padding(8.dp)
//        ) {
//            Icon(
//                imageVector = ImageVector.vectorResource(id = R.drawable.ic_telegram),
//                contentDescription = "Open Telegram",
//                tint = MaterialTheme.colorScheme.primary
//            )
//            Spacer(modifier = Modifier.width(4.dp))
//            Text(
//                text = stringResource(id = R.string.community),
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface,
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                context.getString(R.string.github_link).toUri()
                            )
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            CrashlyticsLogger.logException(e, "Failed to open GitHub link")
                        }
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
                    contentDescription = "Open GitHub",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = context.getString(R.string.by_author),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            NiaTextButton(
                onClick = { uriHandler.openUri(context.getString(R.string.LICENSE)) },
            ) {
                Text(
                    text = stringResource(id = R.string.license),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun SettingsDialogSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
fun SettingsDialogRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun NiaTextButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TextButton(onClick = onClick) {
        content()
    }
}

fun getLanguageDisplayName(languageCode: String, context: android.content.Context): String {
    return LanguageViewModel.getSupportedLanguages(context)
        .find { it.code == languageCode }?.displayName
        ?: context.getString(R.string.system_language)
}

fun getDnsDisplayName(dnsCode: String): String {
    return DnsViewModel.getSupportedDnsServers()
        .find { it.code == dnsCode }?.displayName
        ?: dnsCode
}

@Preview
@Composable
private fun PreviewCustomSettingsDialog() {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager(context)

    SettingsDialog(
        onDismiss = {},
        preferencesManager = preferencesManager,
        onDnsSelected = { },
        themeViewModel = ThemeViewModel(preferencesManager),
        autoConnectViewModel = AutoConnectViewModel(preferencesManager),
        languageViewModel = LanguageViewModel(context),
        dnsViewModel = DnsViewModel(context)
    )
}
