@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)
package cc.chenhe.weargallery.ui.preference

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SaveAs
import androidx.compose.material.icons.rounded.Upgrade
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.common.util.GITHUB
import cc.chenhe.weargallery.common.util.getActivity
import cc.chenhe.weargallery.common.util.getVersionCode
import cc.chenhe.weargallery.common.util.getVersionName
import cc.chenhe.weargallery.common.util.isAlwaysDenied
import cc.chenhe.weargallery.common.util.openMarket
import cc.chenhe.weargallery.common.util.openWithBrowser
import cc.chenhe.weargallery.ui.components.ActionRow
import cc.chenhe.weargallery.ui.components.AppBackButton
import cc.chenhe.weargallery.ui.components.PreferenceRow
import cc.chenhe.weargallery.ui.components.SegmentedColumn
import cc.chenhe.weargallery.ui.theme.WearGalleryTheme
import cc.chenhe.weargallery.utils.NotificationUtils
import cc.chenhe.weargallery.utils.NotificationUtils.Companion.CHANNEL_ID_FOREGROUND_SERVICE
import org.koin.androidx.compose.koinViewModel

@Composable
fun PreferenceScreen(
    navUp: () -> Unit,
    navToAbout: () -> Unit,
    viewModel: PreferenceViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val ob = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.sendIntent(PreferenceIntent.RecheckNotificationState)
            }
        }
        lifecycleOwner.lifecycle.addObserver(ob)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(ob)
        }
    }
    PreferenceScreenFrame(
        navUp,
        navToAbout,
        uiState = uiState,
        onIntent = { viewModel.sendIntent(it) },
    )

    val exportLogState = uiState.exportLogState
    if (exportLogState is PreferenceUiState.ExportLogState.Prepared) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, exportLogState.uri)
            type = "application/zip"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        LocalContext.current.startActivity(Intent.createChooser(shareIntent, null))
        viewModel.sendIntent(PreferenceIntent.ResetExportLogState)
    }
}

@Composable
@Preview
private fun PreferenceScreenFramePreview() {
    WearGalleryTheme {
        PreferenceScreenFrame(
            navUp = {},
            navToAbout = {},
            uiState = PreferenceUiState(exportLogState = PreferenceUiState.ExportLogState.Preparing),
            onIntent = {},
        )
    }
}

@Composable
private fun PreferenceScreenFrame(
    navUp: () -> Unit,
    navToAbout: () -> Unit,
    uiState: PreferenceUiState,
    onIntent: (PreferenceIntent) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.pref_title)) },
                navigationIcon = { AppBackButton(navUp) },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            item {
                SegmentedColumn(title = stringResource(R.string.pref_category_regular)) {
                    item(key = "tip_watch") { shape ->
                        PreferenceRow(
                            title = stringResource(R.string.pref_phone_show_tips_about_watch),
                            icon = Icons.Rounded.Watch,
                            checked = uiState.tipWhenWatchOperating,
                            shape = shape,
                            onCheckedChange = {
                                onIntent(PreferenceIntent.SetTipWhenWatchOperating(it))
                            },
                        )
                    }
                    item(key = "foreground_service") { shape ->
                        PreferenceRow(
                            title = stringResource(R.string.pref_foreground_service),
                            summary = stringResource(R.string.pref_foreground_service_summary),
                            icon = Icons.Rounded.PlayCircle,
                            checked = uiState.foregroundService,
                            shape = shape,
                            onCheckedChange = { onIntent(PreferenceIntent.SetForegroundService(it)) },
                        )
                    }
                    item(key = "foreground_notification", visible = uiState.foregroundService) { shape ->
                        val ctx = LocalContext.current
                        ActionRow(
                            title = stringResource(R.string.pref_foreground_service_notification),
                            icon = Icons.Rounded.Notifications,
                            enabled = uiState.overallNotification,
                            shape = shape,
                            onClick = { ctx.startNotificationChannelSettings(CHANNEL_ID_FOREGROUND_SERVICE) },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                val ctx = LocalContext.current
                val notificationPermissionRequester =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                            if (!granted && ctx.getActivity()
                                    ?.isAlwaysDenied(Manifest.permission.POST_NOTIFICATIONS) == true
                            ) {
                                ctx.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", ctx.packageName, null)
                                    }
                                )
                        } else {
                            onIntent(PreferenceIntent.RecheckNotificationState)
                        }
                    }

                SegmentedColumn(title = stringResource(R.string.pref_category_notification)) {
                    if (!uiState.overallNotification) {
                        item(key = "notification_permission") { shape ->
                            ActionRow(
                                title = stringResource(R.string.pref_notification_overall),
                                icon = Icons.Rounded.Notifications,
                                shape = shape,
                                onClick = {
                                    when {
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                                            notificationPermissionRequester.launch(Manifest.permission.POST_NOTIFICATIONS)

                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> ctx.startActivity(
                                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                                            }
                                        )

                                        else -> ctx.startActivity(
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.fromParts("package", ctx.packageName, null)
                                            }
                                        )
                                    }
                                },
                            )
                        }
                    }

                    item(key = "channel_progress") { shape ->
                        NotificationChannelRow(
                            shape = shape,
                            overall = uiState.overallNotification,
                            title = stringResource(R.string.pref_notification_send_images_progress),
                            channelId = NotificationUtils.CHANNEL_ID_SENDING,
                            enabled = uiState.sendImagesProgressNotification,
                        )
                    }
                    item(key = "channel_result") { shape ->
                        NotificationChannelRow(
                            shape = shape,
                            overall = uiState.overallNotification,
                            title = stringResource(R.string.pref_notification_send_images_result),
                            channelId = NotificationUtils.CHANNEL_ID_SEND_RESULT,
                            enabled = uiState.sendImagesResultNotification,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                val ctx = LocalContext.current
                val versionText = stringResource(
                    R.string.pref_version, getVersionName(ctx), getVersionCode(ctx)
                )
                SegmentedColumn(title = stringResource(R.string.pref_category_other)) {
                    item(key = "check_update") { shape ->
                        ActionRow(
                            title = stringResource(R.string.setting_check_update),
                            summary = versionText,
                            icon = Icons.Rounded.Upgrade,
                            shape = shape,
                            onClick = {
                                openMarket(ctx) { openWithBrowser(ctx, GITHUB) }
                            },
                        )
                    }
                    item(key = "about") { shape ->
                        ActionRow(
                            title = stringResource(R.string.pref_about),
                            icon = Icons.Rounded.Info,
                            shape = shape,
                            onClick = navToAbout,
                        )
                    }
                    item(key = "debug_log") { shape ->
                        ActionRow(
                            title = stringResource(R.string.pref_send_debug_log),
                            icon = Icons.Rounded.Code,
                            shape = shape,
                            enabled = uiState.exportLogState != PreferenceUiState.ExportLogState.Preparing,
                            onClick = { onIntent(PreferenceIntent.SendLog) },
                            trailing = {
                                when (val state = uiState.exportLogState) {
                                    PreferenceUiState.ExportLogState.Empty -> {
                                        Text(
                                            text = stringResource(R.string.pref_export_debug_log_empty),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    PreferenceUiState.ExportLogState.Preparing -> {
                                        LoadingIndicator(modifier = Modifier.size(24.dp))
                                    }

                                    else -> {
                                        val launcher = rememberLauncherForActivityResult(
                                            ActivityResultContracts.CreateDocument("application/zip")
                                        ) { uri ->
                                            if (uri != null) onIntent(PreferenceIntent.SaveLog(uri))
                                        }
                                        IconButton(onClick = { launcher.launch("weargallery_log.zip") }) {
                                            Icon(
                                                Icons.Rounded.SaveAs,
                                                contentDescription = stringResource(R.string.pref_save_debug_log),
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun NotificationChannelRow(
    shape: androidx.compose.ui.graphics.Shape,
    overall: Boolean,
    title: String,
    channelId: String,
    enabled: Boolean,
) {
    val ctx = LocalContext.current
    ActionRow(
        title = title,
        icon = if (channelId == NotificationUtils.CHANNEL_ID_SENDING) {
            Icons.Rounded.Pending
        } else {
            Icons.Rounded.CheckCircle
        },
        shape = shape,
        enabled = overall,
        onClick = { ctx.startNotificationChannelSettings(channelId) },
        trailing = {
            androidx.compose.material3.Switch(
                checked = overall && enabled,
                enabled = overall,
                onCheckedChange = null,
                thumbContent = {
                    Icon(
                        imageVector = if (overall && enabled) {
                            Icons.Rounded.Check
                        } else {
                            Icons.Rounded.Close
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        },
    )
}

private fun Context.startNotificationChannelSettings(channelId: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startActivity(
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            }
        )
    }
}
