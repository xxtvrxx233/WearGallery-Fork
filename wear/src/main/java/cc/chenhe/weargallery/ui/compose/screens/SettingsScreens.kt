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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.transformedHeight
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.common.util.GITHUB
import cc.chenhe.weargallery.common.util.openMarket
import cc.chenhe.weargallery.ui.compose.WearRoute
import cc.chenhe.weargallery.ui.compose.components.WearActionRow
import cc.chenhe.weargallery.ui.compose.components.WearConfirmDialog
import cc.chenhe.weargallery.ui.compose.components.WearMessageScreen
import cc.chenhe.weargallery.ui.compose.components.WearPageHeader
import cc.chenhe.weargallery.ui.compose.components.WearPageList
import cc.chenhe.weargallery.ui.explore.AboutViewModel
import cc.chenhe.weargallery.uilts.ACTION_WATCH_FACE_BACKGROUND_CHANGED
import cc.chenhe.weargallery.uilts.WATCH_FACE_BACKGROUND
import cc.chenhe.weargallery.uilts.fetchKeepScreenOn
import cc.chenhe.weargallery.uilts.fetchShowPhoneImages
import cc.chenhe.weargallery.uilts.fetchWatchFaceDimImage
import cc.chenhe.weargallery.uilts.fetchWatchFaceStyle
import cc.chenhe.weargallery.uilts.fetchWatchFaceTimeX
import cc.chenhe.weargallery.uilts.fetchWatchFaceTimeY
import cc.chenhe.weargallery.uilts.fetchWatchFaceTimeTextSize
import cc.chenhe.weargallery.uilts.getWatchFaceResFolder
import cc.chenhe.weargallery.uilts.setWatchFaceTextSize
import cc.chenhe.weargallery.uilts.setWatchFaceTimePosition
import cc.chenhe.weargallery.uilts.showPhoneImages
import cc.chenhe.weargallery.uilts.ZxingUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import java.io.File
import java.io.FileOutputStream

private const val PREF_WATCHFACE_DIM = "preference_watchface_dim"
private const val PREF_WATCHFACE_STYLE = "watchface_type"

private fun setBooleanPreference(context: Context, key: String, value: Boolean) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply()
}

private fun setStringPreference(context: Context, key: String, value: String) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
}

// ---------------------------------------------------------------------------
// Display
// ---------------------------------------------------------------------------

@Composable
fun DisplaySettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val showPhone by fetchShowPhoneImages(context).observeAsState(true)
    val keepScreenOn by fetchKeepScreenOn(context).observeAsState(false)

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.preference_display)) }
        item {
            SwitchButton(
                checked = showPhone,
                onCheckedChange = { showPhoneImages(context, shouldQuery = false, show = it) },
                label = { Text(stringResource(R.string.preference_show_phone_images)) },
                modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                transformation = SurfaceTransformation(spec),
            )
        }
        item {
            SwitchButton(
                checked = keepScreenOn,
                onCheckedChange = {
                    setBooleanPreference(context, cc.chenhe.weargallery.uilts.PREFERENCE_KEEP_SCREEN_ON, it)
                },
                label = { Text(stringResource(R.string.preference_always_on)) },
                secondaryLabel = { Text(stringResource(R.string.preference_always_on_summary)) },
                modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                transformation = SurfaceTransformation(spec),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Transfer
// ---------------------------------------------------------------------------

@Composable
fun TransferSettingsScreen(onNavigate: (WearRoute) -> Unit) {
    var showDisclaimer by remember { mutableStateOf(false) }
    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.preference_transfer)) }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.preference_transfer_lan),
                onClick = { onNavigate(WearRoute.WebServer) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.preference_transfer_disclaimer),
                onClick = { showDisclaimer = true },
            )
        }
    }
    if (showDisclaimer) {
        InfoDialog(
            title = stringResource(R.string.preference_transfer_disclaimer),
            message = stringResource(R.string.preference_transfer_disclaimer_content),
            onDismiss = { showDisclaimer = false },
        )
    }
}

@Composable
internal fun InfoDialog(title: String, message: String, onDismiss: () -> Unit) {
    androidx.wear.compose.material3.AlertDialog(
        visible = true,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.ok))
            }
        },
        onDismissRequest = onDismiss,
    )
}

// ---------------------------------------------------------------------------
// Watch face settings
// ---------------------------------------------------------------------------

@Composable
fun WatchFaceSettingsScreen(onNavigate: (WearRoute) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val style by fetchWatchFaceStyle(context, true).observeAsState()
    val dim by fetchWatchFaceDimImage(context, true).observeAsState(false)
    var confirmDim by remember { mutableStateOf(false) }
    var pendingDim by remember { mutableStateOf(false) }

    val analog = stringResource(R.string.wf_preference_type_entry_value_analog)
    val digital = stringResource(R.string.wf_preference_type_entry_value_digital)

    // Use the app's own gallery picker: this is a gallery app, asking the system picker for a
    // local picture would be backwards.
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    copyWatchFaceBackground(context, uri)
                }
            }
        }
    }

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.wf_preference_title)) }
        item {
            SwitchButton(
                checked = dim,
                onCheckedChange = { checked ->
                    if (!checked || dim) {
                        setBooleanPreference(context, PREF_WATCHFACE_DIM, checked)
                    } else {
                        pendingDim = true
                        confirmDim = true
                    }
                },
                label = { Text(stringResource(R.string.wf_preference_dim)) },
                modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                transformation = SurfaceTransformation(spec),
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.wf_preference_image),
                onClick = {
                    imagePicker.launch(
                        android.content.Intent(context, cc.chenhe.weargallery.ui.pick.PickImageAty::class.java)
                    )
                },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.wf_preference_type),
                secondaryLabel = if (style == digital) digital else analog,
                onClick = {
                    setStringPreference(
                        context,
                        PREF_WATCHFACE_STYLE,
                        if (style == digital) analog else digital,
                    )
                },
            )
        }
        if (style == digital) {
            item {
                WearActionRow(
                    spec,
                    label = stringResource(R.string.wf_preference_position),
                    onClick = { onNavigate(WearRoute.WatchFaceText("position")) },
                )
            }
            item {
                WearActionRow(
                    spec,
                    label = stringResource(R.string.wf_preference_format),
                    onClick = { onNavigate(WearRoute.WatchFaceText("format")) },
                )
            }
            item {
                WearActionRow(
                    spec,
                    label = stringResource(R.string.wf_preference_color),
                    onClick = { onNavigate(WearRoute.WatchFaceText("color")) },
                )
            }
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.wf_preference_cot),
                onClick = { openCotStore(context) },
            )
        }
    }

    if (confirmDim) {
        WearConfirmDialog(
            title = stringResource(R.string.tip),
            message = stringResource(R.string.wf_dim_dialog),
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                pendingDim = false
                setBooleanPreference(context, PREF_WATCHFACE_DIM, true)
            },
            onDismiss = { confirmDim = false },
        )
    }
}

private fun openCotStore(context: Context) {
    val uri = Uri.parse("market://details?id=com.cotwf.watchfaceplatform.wearos")
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private suspend fun copyWatchFaceBackground(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
    val target = File(getWatchFaceResFolder(context), WATCH_FACE_BACKGROUND)
    if (target.isFile) target.delete()
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(target).use { output -> input.copyTo(output) }
    }
    LocalBroadcastManager.getInstance(context)
        .sendBroadcast(Intent(ACTION_WATCH_FACE_BACKGROUND_CHANGED))
}

// ---------------------------------------------------------------------------
// Watch face text style
// ---------------------------------------------------------------------------

@Composable
fun WatchFaceTextScreen(kind: String, onBack: () -> Unit) {
    when (kind) {
        "position" -> WatchFacePositionScreen()
        "format" -> WatchFaceFormatScreen()
        else -> WatchFaceColorScreen()
    }
}

@Composable
private fun WatchFacePositionScreen() {
    val context = LocalContext.current
    val x by fetchWatchFaceTimeX(context, true).observeAsState(-1f)
    val y by fetchWatchFaceTimeY(context, true).observeAsState(-1f)
    val size by fetchWatchFaceTimeTextSize(context, true).observeAsState(0f)
    val startX = if (x < 0f) 0.5f else x
    val startY = if (y < 0f) 0.5f else y

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.wf_preference_position)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StyleIcon(R.drawable.ic_wf_pos_left, stringResource(R.string.wf_position_left)) {
                    setWatchFaceTimePosition(context, (startX - 0.02f).coerceAtLeast(0f), startY)
                }
                StyleIcon(R.drawable.ic_wf_pos_right, stringResource(R.string.wf_position_right)) {
                    setWatchFaceTimePosition(context, (startX + 0.02f).coerceAtMost(1f), startY)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StyleIcon(R.drawable.ic_wf_pos_up, stringResource(R.string.wf_position_up)) {
                    setWatchFaceTimePosition(context, startX, (startY - 0.02f).coerceAtLeast(0f))
                }
                StyleIcon(R.drawable.ic_wf_pos_down, stringResource(R.string.wf_position_down)) {
                    setWatchFaceTimePosition(context, startX, (startY + 0.02f).coerceAtMost(1f))
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StyleIcon(R.drawable.ic_wf_size_decrease, stringResource(R.string.wf_size_smaller)) {
                    setWatchFaceTextSize(context, (size - 4f).coerceAtLeast(12f))
                }
                StyleIcon(R.drawable.ic_wf_size_increase, stringResource(R.string.wf_size_larger)) {
                    setWatchFaceTextSize(context, size + 4f)
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.wf_preference_position),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WatchFaceFormatScreen() {
    val context = LocalContext.current
    val model: cc.chenhe.weargallery.watchface.style.TimeTextFormatViewModel = koinViewModel()
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        model.loadPreference()
        tags = model.tags.toList()
    }

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.wf_preference_format)) }
        item {
            Text(
                text = tags.joinToString(" ") { it },
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        model.tagsMap.values.forEach { tag ->
            item(key = "add-$tag") {
                WearActionRow(
                    spec,
                    label = "+ " + stringResource(model.tagsNameIds.getValue(tag)),
                    onClick = {
                        model.addTag(tag)
                        tags = model.tags.toList()
                    },
                )
            }
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.wf_preference_format_reset),
                onClick = {
                    model.resetPreference()
                    tags = model.tags.toList()
                },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.confirm),
                onClick = { model.savePreference() },
            )
        }
    }
}

@Composable
private fun WatchFaceColorScreen() {
    val context = LocalContext.current
    val model: cc.chenhe.weargallery.watchface.style.TimeTextColorViewModel = koinViewModel()
    val colors = listOf(
        Color.White, Color.Black, Color.Red, Color.Green, Color.Blue,
        Color.Yellow, Color.Cyan, Color.Magenta,
    )
    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.wf_preference_color)) }
        colors.chunked(4).forEach { row ->
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    model.setColor(color.value.toInt())
                                    model.saveColor()
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StyleIcon(
    @androidx.annotation.DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(icon),
            contentDescription = description,
            modifier = Modifier.size(24.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// About
// ---------------------------------------------------------------------------

@Composable
fun AboutScreen(onNavigate: (WearRoute) -> Unit) {
    val context = LocalContext.current
    val model: AboutViewModel = koinViewModel()
    val newVersion by model.newVersion.observeAsState(2)
    val buttonTitle by model.checkUpdateBtnTitle.observeAsState("")
    var showQr by remember { mutableStateOf<String?>(null) }
    var thanks by remember { mutableStateOf(false) }

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.preference_about)) }
        item {
            Text(
                text = cc.chenhe.weargallery.common.util.getVersionName(context),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        item {
            WearActionRow(
                spec,
                label = buttonTitle,
                onClick = {
                    if (newVersion == 1) {
                        openMarket(context) { showQr = model.url }
                    } else {
                        model.checkUpdate()
                    }
                },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.about_licence),
                onClick = { onNavigate(WearRoute.Licenses) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.about_thank),
                onClick = { thanks = true },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.about_github),
                onClick = { showQr = GITHUB },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.about_telegram),
                onClick = { showQr = TELEGRAM_URL },
            )
        }
    }

    if (thanks) {
        InfoDialog(
            title = stringResource(R.string.about_thank),
            message = stringResource(R.string.about_thank_content),
            onDismiss = { thanks = false },
        )
    }
    showQr?.let { url ->
        ScanDialog(url = url, onDismiss = { showQr = null })
    }
}

private const val TELEGRAM_URL = "https://t.me/weargallery_news"

@Composable
private fun ScanDialog(url: String, onDismiss: () -> Unit) {
    val bitmap = remember(url) {
        runCatching { ZxingUtils.generateBitmap(url, 300, 300) }.getOrNull()
    }
    androidx.wear.compose.material3.AlertDialog(
        visible = true,
        title = { Text(stringResource(R.string.dialog_scan_to_visit)) },
        text = {
            bitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = url)
            }
        },
        confirmButton = {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.ok))
            }
        },
        onDismissRequest = onDismiss,
    )
}

// ---------------------------------------------------------------------------
// Licenses
// ---------------------------------------------------------------------------

private data class License(val name: String, val author: String, val type: String)

private val LICENSES = listOf(
    License("Android Open Source Project", "AOSP", "Apache 2.0"),
    License("Coil", "coil-kt", "Apache 2.0"),
    License("Koin", "InsertKoinIO", "Apache 2.0"),
    License("Kotlin", "JetBrains", "Apache 2.0"),
    License("Moshi", "Square", "Apache 2.0"),
    License("NanoHTTPD", "NanoHTTPD", "BSD 3-Clause"),
    License("Sketch", "panpf", "Apache 2.0"),
    License("WatchFaceHelper", "Chenhe", "Apache 2.0"),
    License("Wear-Msger", "Chenhe", "MIT"),
    License("Wear-Vision", "Chenhe", "Apache 2.0"),
)

@Composable
fun LicensesScreen() {
    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.about_licence)) }
        LICENSES.forEach { license ->
            item(key = license.name) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec),
                ) {
                    Text(license.name, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${license.author} · ${license.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
