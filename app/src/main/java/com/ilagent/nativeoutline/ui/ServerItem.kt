package com.ilagent.nativeoutline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ilagent.nativeoutline.R
import com.ilagent.nativeoutline.data.preferences.PreferencesManager
import com.ilagent.nativeoutline.data.remote.IpCountryCodeProvider
import com.ilagent.nativeoutline.data.remote.RemoteJSONFetch
import com.ilagent.nativeoutline.data.remote.ServerIconProvider
import com.ilagent.nativeoutline.viewmodel.ServerItemViewModel

@Composable
fun ServerItem(
    serverName: String,
    serverHost: String,
    onForwardIconClick: () -> Unit,
) {
    val context = LocalContext.current

    val viewModel: ServerItemViewModel = viewModel(
        factory = ServerItemViewModel.Factory(
            serverIconProvider = ServerIconProvider.FlagsApiDotCom(
                ipCountryCodeProvider = IpCountryCodeProvider.IpApiDotCo(
                    fetch = RemoteJSONFetch.HttpURLConnectionJSONFetch()
                ),
                preferencesManager = PreferencesManager(context = context),
            )
        )
    )

    val serverIconState by viewModel.serverIconState.observeAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(serverHost) { viewModel.serverHost(serverHost) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                width = 3.dp,
                color = if (isFocused)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                else
                    Color.Transparent,
                shape = MaterialTheme.shapes.large
            )
            .padding(4.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = onForwardIconClick
            )
            .padding(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            if (serverIconState != null) {
                AsyncImage(
                    model = serverIconState,
                    contentDescription = "Server icon",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = FixedScale(3f),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = serverName.ifEmpty { context.getString(R.string.server_not_specified) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface, // Текст по умолчанию на фоне
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (serverHost.isNotBlank()) {
                    Text(
                        text = serverHost,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { onForwardIconClick() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
fun ServerItemPreview() {
    ServerItem(
        serverName = "Server",
        serverHost = "0.0.0.0",
        onForwardIconClick = {}
    )
}

@Preview
@Composable
fun ServerEmptyItemPreview() {
    ServerItem(
        serverName = "",
        serverHost = "",
        onForwardIconClick = {}
    )
}
