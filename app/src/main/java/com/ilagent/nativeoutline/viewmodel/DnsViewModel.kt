package com.ilagent.nativeoutline.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.ilagent.nativeoutline.data.preferences.PreferencesManager

class DnsViewModel(context: Context) : ViewModel() {
    private val preferencesManager = PreferencesManager(context)

    private val _dnsCode = mutableStateOf(
        preferencesManager.getSelectedDns() ?: "8.8.8.8"
    )
    val dnsCode: State<String> = _dnsCode

    fun setDns(dnsCode: String) {
        if (_dnsCode.value == dnsCode) {
            return // DNS hasn't changed
        }

        preferencesManager.saveSelectedDns(dnsCode)
        _dnsCode.value = dnsCode
    }

    companion object {
        fun getSupportedDnsServers(): List<DnsServer> {
            return listOf(
                DnsServer("8.8.8.8", "Google DNS"),
                DnsServer("1.1.1.1", "Cloudflare DNS"),
                DnsServer("77.88.8.8", "Yandex DNS"),
                DnsServer("94.140.14.14", "AdGuard DNS"),
                DnsServer("208.67.222.222", "OpenDNS"),
                DnsServer("9.9.9.9", "Quad9 DNS"),
                DnsServer("8.26.56.26", "Comodo Secure DNS")
            )
        }
    }
}

data class DnsServer(
    val code: String,
    val displayName: String
)