/**
 * Copyright (C) 2026 xxtvrxx233
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cc.chenhe.weargallery.ui.compose.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Text
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.ui.compose.components.WearActionRow
import cc.chenhe.weargallery.ui.compose.components.WearPageHeader
import cc.chenhe.weargallery.ui.compose.components.WearPageList
import cc.chenhe.weargallery.ui.webserver.WebServerViewModel
import cc.chenhe.weargallery.uilts.ZxingUtils
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WebServerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val model: WebServerViewModel = koinViewModel()
    val networkState by model.networkState.observeAsState(WebServerViewModel.NETWORK_STATE_TRYING)
    val serverState by model.serverState.observeAsState(WebServerViewModel.SERVER_STATE_STOP)
    val serverIp by model.serverIp.observeAsState()
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { model.checkNetwork() }

    LaunchedEffect(networkState) {
        when (networkState) {
            WebServerViewModel.NETWORK_STATE_AVAILABLE -> model.startServer()
            WebServerViewModel.NETWORK_STATE_TRYING,
            WebServerViewModel.NETWORK_STATE_TIMEOUT -> model.stopServer()
        }
    }

    DisposableEffect(Unit) {
        onDispose { model.stopServer() }
    }

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.preference_transfer_lan)) }

        if (networkState != WebServerViewModel.NETWORK_STATE_AVAILABLE) {
            item {
                Text(
                    text = stringResource(
                        if (networkState == WebServerViewModel.NETWORK_STATE_TIMEOUT) {
                            R.string.server_timeout
                        } else {
                            R.string.server_trying
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            item {
                WearActionRow(
                    spec,
                    label = stringResource(R.string.server_add_wifi),
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                        }
                    },
                )
            }
        } else {
            val ip = serverIp
            if (ip.isNullOrEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.server_trying),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                item {
                    val qr = remember(ip) {
                        ZxingUtils.generateBitmap("http://$ip", 300, 300)
                    }
                    Image(
                        bitmap = qr.asImageBitmap(),
                        contentDescription = ip,
                        modifier = Modifier.fillMaxWidth(0.7f),
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.server_ip, ip),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            item {
                Text(
                    text = model.getServerStateMsg(serverState) ?: "",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = androidx.compose.ui.graphics.Color(
                        model.getServerStateColor(serverState)
                    ),
                )
            }
            item {
                WearActionRow(
                    spec,
                    label = stringResource(R.string.server_help),
                    onClick = { showHelp = true },
                )
            }
        }
    }

    if (showHelp) {
        InfoDialog(
            title = stringResource(R.string.tip),
            message = stringResource(R.string.server_help_msg),
            onDismiss = { showHelp = false },
        )
    }
}
