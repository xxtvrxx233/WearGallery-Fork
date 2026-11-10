/**
 * Copyright (C) 2020 Chenhe
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

package cc.chenhe.weargallery.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.ui.compose.WearGalleryTheme
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import kotlinx.coroutines.launch

/** Intro carousel with the runtime storage permission request. */
class IntroduceAty : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearGalleryTheme {
                AppScaffold(timeText = { TimeText() }) {
                    IntroduceScreen(
                        onGranted = { finish() },
                        onOpenSettings = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null),
                                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                            finish()
                        },
                    )
                }
            }
        }
    }
}

/** The runtime permissions that grant read access to the picture library. */
private fun requestedStoragePermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= 34 -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
    )

    Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    Build.VERSION.SDK_INT > Build.VERSION_CODES.Q -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    else -> arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
}

private data class IntroSlide(val title: Int, val content: Int)

private val SLIDES = listOf(
    IntroSlide(R.string.intro_gif_title, R.string.intro_gif_content),
    IntroSlide(R.string.intro_lan_title, R.string.intro_lan_content),
    IntroSlide(R.string.intro_wf_title, R.string.intro_wf_content),
    IntroSlide(R.string.intro_os_title, R.string.intro_os_content),
)

@Composable
private fun IntroduceScreen(onGranted: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { SLIDES.size })
    var denied = remember { androidx.compose.runtime.mutableStateOf(false) }

    val permissions = remember { requestedStoragePermissions() }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (cc.chenhe.weargallery.uilts.checkStoragePermissions(context)) {
            onGranted()
        } else {
            denied.value = true
        }
    }

    // Ask as soon as the intro opens instead of hiding the request behind the last slide.
    LaunchedEffect(Unit) {
        if (!cc.chenhe.weargallery.uilts.checkStoragePermissions(context)) {
            launcher.launch(permissions)
        }
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            val slide = SLIDES[page]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(slide.title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(slide.content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        IconButton(
            onClick = {
                if (pagerState.currentPage < SLIDES.lastIndex) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    launcher.launch(permissions)
                }
            },
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            Icon(
                imageVector = if (pagerState.currentPage < SLIDES.lastIndex) {
                    Icons.Rounded.ArrowForward
                } else {
                    Icons.Rounded.Check
                },
                contentDescription = if (pagerState.currentPage < SLIDES.lastIndex) {
                    stringResource(R.string.ok)
                } else {
                    stringResource(R.string.intro_grant_permission)
                },
            )
        }
    }

    if (denied.value) {
        AlertDialog(
            visible = true,
            title = { Text(stringResource(R.string.local_err_permission_title)) },
            text = { Text(stringResource(R.string.local_err_permission_content)) },
            confirmButton = {
                IconButton(
                    onClick = {
                        denied.value = false
                        launcher.launch(permissions)
                    },
                    colors = androidx.wear.compose.material3.IconButtonDefaults.filledIconButtonColors(),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.retry))
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        denied.value = false
                        onOpenSettings()
                    },
                    colors = androidx.wear.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(),
                ) {
                    Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.intro_open_settings))
                }
            },
            onDismissRequest = { denied.value = false },
        )
    }
}
