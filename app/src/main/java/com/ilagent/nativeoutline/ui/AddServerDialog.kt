package com.ilagent.nativeoutline.ui

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.ParseUrlOutline
import com.ilagent.nativeoutline.utils.CrashlyticsLogger
import com.ilagent.nativeoutline.viewmodel.ServerDialogViewModel

/**
 * Dialog for adding a new VPN server.
 * Contains fields for server name and outline key,
 * with buttons to import from clipboard, file, or QR code.
 * @param initialAction - initial action to perform: "clipboard", "file", "qr", or null
 */
@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    initialAction: String? = null
) {
    val viewModel: ServerDialogViewModel = viewModel(
        factory = ServerDialogViewModel.Factory(ParseUrlOutline.Validate.Base())
    )

    var serverName by remember { mutableStateOf("") }
    var serverKey by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isKeyError by remember { mutableStateOf(false) }
    var showQrPair by remember { mutableStateOf(initialAction == "qr") }

    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val preferencesManager = PreferencesManager(context)

    // Log dialog opening
    LaunchedEffect(Unit) {
        CrashlyticsLogger.logServerDialogOpened()
    }

    fun validateKey(key: String) {
        isKeyError = !viewModel.validate(key)
        if (isKeyError && key.isNotBlank()) {
            CrashlyticsLogger.logServerKeyValidationError("invalid_format")
        }
    }

    fun setServerKey(key: String) {
        serverKey = key
        validateKey(key)
    }

    // Launcher for Storage Access Framework (SAF) - works on all devices
    val safFilePickerLauncher = rememberLauncherForActivityResult(
        contract = GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Check file size (max 100 KB for Outline key)
                val fileSize =
                    context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst() && sizeIndex != -1) {
                            cursor.getLong(sizeIndex)
                        } else null
                    }

                val maxFileSize = 10240L
                if (fileSize != null && fileSize > maxFileSize) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_file_too_large),
                        Toast.LENGTH_LONG
                    ).show()
                    return@let
                }

                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val data = inputStream.bufferedReader().readText().trim()
                    if (data.isNotBlank()) {
                        // Limit text length from file
                        val trimmedData = if (data.length > 2000) {
                            data.substring(0, 2000)
                        } else {
                            data
                        }
                        setServerKey(trimmedData)
                        CrashlyticsLogger.logServerImportedFromFile()
                    }
                }
            } catch (e: Exception) {
                CrashlyticsLogger.logException(e, "Failed to read file from SAF")
                Toast.makeText(
                    context,
                    context.getString(R.string.error_reading_file, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Handle initial action when dialog opens
    LaunchedEffect(initialAction) {
        when (initialAction) {
            "clipboard" -> {
                val clipboardText = clipboardManager.getText()?.text
                if (!clipboardText.isNullOrEmpty()) {
                    val trimmedText = if (clipboardText.length > 2000) {
                        clipboardText.substring(0, 2000)
                    } else {
                        clipboardText
                    }
                    setServerKey(trimmedText)
                    CrashlyticsLogger.logServerImportedFromClipboard()
                } else {
                    Toast.makeText(context, R.string.clipboard_empty, Toast.LENGTH_SHORT).show()
                }
            }

            "file" -> {
                try {
                    safFilePickerLauncher.launch("*/*")
                } catch (e: Exception) {
                    CrashlyticsLogger.logException(e, "SAF file picker launch failed")
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_file_access_unavailable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            "qr" -> {
                // QR dialog is already opened via showQrPair initial state
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) {
                CrashlyticsLogger.logServerAddCancelled()
                onDismiss()
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.add_new_server),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            CrashlyticsLogger.logServerImportedFromClipboard()
                            val clipboardText = clipboardManager.getText()?.text
                            if (!clipboardText.isNullOrEmpty()) {
                                // Limit text length from clipboard
                                val trimmedText = if (clipboardText.length > 2000) {
                                    clipboardText.substring(0, 2000)
                                } else {
                                    clipboardText
                                }
                                setServerKey(trimmedText)
                            } else {
                                Toast.makeText(
                                    context,
                                    R.string.clipboard_empty,
                                    Toast.LENGTH_SHORT
                                ).show()
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
                                // Always use Storage Access Framework (SAF)
                                // Works on all Android devices without special permissions
                                // "*/*" allows selecting any files, including without extension
                                safFilePickerLauncher.launch("*/*")
                            } catch (e: Exception) {
                                CrashlyticsLogger.logException(e, "SAF file picker launch failed")
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_file_access_unavailable),
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
                        onClick = {
                            CrashlyticsLogger.logQrScanStarted()
                            showQrPair = true
                        }
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
                OutlinedTextField(
                    value = serverName,
                    onValueChange = {
                        if (it.length <= 1000) {
                            serverName = it
                        }
                    },
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
                        // Limit key length to 2000 characters to prevent OOM
                        if (it.length <= 2000) {
                            setServerKey(it)
                        }
                    },
                    label = { Text(stringResource(id = R.string.outline_key)) },
                    singleLine = true,
                    maxLines = 1,
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
                    if (!isLoading) {
                        CrashlyticsLogger.logServerAddCancelled()
                        onDismiss()
                    }
                }) {
                    Text(stringResource(id = R.string.cancel))
                }

                TextButton(
                    onClick = {
                        isLoading = true
                        try {
                            val finalName = serverName.ifEmpty {
                                preferencesManager.generateDefaultServerName()
                            }
                            CrashlyticsLogger.logServerAdded(finalName)
                            onSave(finalName, serverKey)
                            isLoading = false
                        } catch (e: Exception) {
                            CrashlyticsLogger.logException(e, "Failed to save VPN server")
                            errorMessage = e.message
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && !isKeyError && serverKey.isNotBlank()
                ) {
                    Text(stringResource(id = R.string.save))
                }
            }
        }
    )

    if (showQrPair) {
        PairByQrDialog(
            onKeyReady = { keyFromQr ->
                // Limit key length from QR code
                val trimmedKey = if (keyFromQr.length > 2000) {
                    keyFromQr.substring(0, 2000)
                } else {
                    keyFromQr
                }
                setServerKey(trimmedKey)
                CrashlyticsLogger.logServerImportedFromQr()
                showQrPair = false
                Toast.makeText(context, R.string.key_received, Toast.LENGTH_SHORT).show()
            },
            onCancel = { showQrPair = false }
        )
    }
}

@Preview
@Composable
fun AddServerDialogPreview() {
    AddServerDialog(
        onDismiss = {},
        onSave = { _, _ -> },
    )
}
