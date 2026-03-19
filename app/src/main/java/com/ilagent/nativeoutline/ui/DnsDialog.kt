package com.ilagent.nativeoutline.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.viewmodel.DnsViewModel

@Composable
fun DnsDialog(
    onDismiss: () -> Unit,
    dnsViewModel: DnsViewModel,
    onDnsSelected: (String) -> Unit
) {
    val currentDns by dnsViewModel.dnsCode
    var tempSelectedDns by remember { mutableStateOf(currentDns) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(id = R.string.DNS),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .selectableGroup()
            ) {
                DnsViewModel.getSupportedDnsServers().forEach { dns ->
                    DnsDialogRadioItem(
                        text = dns.displayName,
                        selected = tempSelectedDns == dns.code,
                        onClick = {
                            tempSelectedDns = dns.code
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDnsSelected(tempSelectedDns)
                onDismiss()
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    )
}

@Composable
fun DnsDialogRadioItem(
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

@Preview
@Composable
private fun PreviewDnsDialog() {
    val dnsViewModel = DnsViewModel(androidx.compose.ui.platform.LocalContext.current)

    DnsDialog(
        onDismiss = {},
        dnsViewModel = dnsViewModel,
        onDnsSelected = {}
    )
}