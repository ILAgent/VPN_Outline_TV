package com.ilagent.nativeoutline.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VpnServerInfo(
    val name: String,
    val key: String
)