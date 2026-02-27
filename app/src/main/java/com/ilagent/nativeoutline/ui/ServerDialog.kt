package com.ilagent.nativeoutline.ui

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.ParseUrlOutline
import com.ilagent.nativeoutline.viewmodel.ServerDialogViewModel

@Composable
fun ServerDialog(
    currentName: String,
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val viewModel: ServerDialogViewModel = viewModel(
        factory = ServerDialogViewModel.Factory(ParseUrlOutline.Validate.Base())
    )

    var serverName by remember { mutableStateOf(currentName) }
    var serverKey by remember { mutableStateOf(currentKey) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isKeyError by remember { mutableStateOf(false) }
    var showQrPair by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val preferencesManager = remember { PreferencesManager(context) }
    var savedVpnKeys by remember { mutableStateOf(preferencesManager.getVpnKeys()) }
    var expanded by remember { mutableStateOf(false) }

    fun validateKey(key: String) {
        isKeyError = !viewModel.validate(key)
    }

    fun setServerKey(key: String) {
        serverKey = key
        validateKey(key)
    }

    // Launcher для Storage Access Framework (SAF) - работает на всех устройствах
    val safFilePickerLauncher = rememberLauncherForActivityResult(
        contract = GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Проверяем размер файла (максимум 100 КБ для Outline ключа)
                val fileSize = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst() && sizeIndex != -1) {
                        cursor.getLong(sizeIndex)
                    } else null
                }

                val maxFileSize = 10240L
                if (fileSize != null && fileSize > maxFileSize) {
                    Toast.makeText(
                        context,
                        "Файл слишком большой. Максимальный размер: 100Б",
                        Toast.LENGTH_LONG
                    ).show()
                    return@let
                }

                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val data = inputStream.bufferedReader().readText().trim()
                    if (data.isNotBlank()) {
                        val parsedName = data.substringAfterLast("#", serverName)
                        serverName = parsedName
                        setServerKey(data)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка чтения файла: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.edit_server_info),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {

                    IconButton(
                        onClick = {
                            val clipboardText = clipboardManager.getText()?.text
                            if (!clipboardText.isNullOrEmpty()) {
                                val parsedName = clipboardText.substringAfterLast("#", serverName)
                                serverName = parsedName
                                setServerKey(clipboardText)
                            } else {
                                Toast.makeText(context, R.string.clipboard_empty, Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentPaste,
                            contentDescription = "Paste from clipboard"
                        )
                    }

                    IconButton(
                        onClick = {
                            try {
                                // Всегда используем Storage Access Framework (SAF)
                                // Работает на всех устройствах Android без специальных разрешений
                                // "*/*" позволяет выбирать любые файлы, включая без расширения
                                safFilePickerLauncher.launch("*/*")
                            } catch (e: Exception) {
                                // Если SAF не поддерживается, показываем сообщение
                                Toast.makeText(
                                    context,
                                    "Файловый доступ недоступен. Используйте вставку из буфера обмена или QR-код.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Folder,
                            contentDescription = "Read from file"
                        )
                    }
                    IconButton(
                        onClick = { showQrPair = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode,
                            contentDescription = "Scan via QR"
                        )
                    }
                }
            }
        },
        text = {
            Column {
                Box {
                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        label = { Text(stringResource(id = R.string.saved_vpn_keys)) },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = "Dropdown Menu"
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(id = R.string.add_new_key)) },
                            onClick = {
                                expanded = false
                                serverName = ""
                                setServerKey("")
                            }
                        )

                        savedVpnKeys.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item.name) },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        preferencesManager.deleteVpnKey(item.name)
                                        savedVpnKeys = preferencesManager.getVpnKeys()
                                        serverName = ""
                                        setServerKey("")
                                        expanded = false
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete"
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    serverName = item.name
                                    setServerKey(item.key)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text(stringResource(id = R.string.server_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = serverKey,
                    isError = isKeyError,
                    supportingText = {
                        if (isKeyError) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = context.getString(R.string.wrong_outline_key_format),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    onValueChange = {
                        val parsedName = it.substringAfterLast("#", serverName)
                        serverName = parsedName
                        setServerKey(it)
                    },
                    label = { Text(stringResource(id = R.string.outline_key)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    serverName = context.getString(R.string.default_server_name)
                    serverKey = ""
                }) {
                    Text(stringResource(id = R.string.clear))
                }

                TextButton(onClick = {
                    if (!isLoading) onDismiss()
                }) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(
                    onClick = {
                        isLoading = true
                        try {
                            onSave(serverName, serverKey)
                            preferencesManager.addOrUpdateVpnKey(serverName, serverKey)
                            savedVpnKeys = preferencesManager.getVpnKeys()
                            isLoading = false
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = e.message
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && !isKeyError
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )

    if (showQrPair) {
        PairByQrDialog(
            onKeyReady = { keyFromQr ->
                val parsedName = keyFromQr.substringAfterLast("#", serverName)
                serverName = parsedName
                setServerKey(keyFromQr)
                showQrPair = false
                Toast.makeText(context, R.string.key_received, Toast.LENGTH_SHORT).show()
            },
            onCancel = { showQrPair = false }
        )
    }
}

@Preview
@Composable
fun DialogPreview() {
    ServerDialog(
        currentName = "Server #1",
        currentKey = "",
        onDismiss = {},
        onSave = { _, _ -> },
    )
}
