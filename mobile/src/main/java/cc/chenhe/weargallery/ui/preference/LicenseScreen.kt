package cc.chenhe.weargallery.ui.preference

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import cc.chenhe.weargallery.ui.components.ActionRow
import cc.chenhe.weargallery.ui.components.AppBackButton
import cc.chenhe.weargallery.ui.components.SegmentedColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cc.chenhe.weargallery.ui.theme.ContentAlpha
import org.koin.androidx.compose.koinViewModel

@Composable
fun LicenseScreen(navUp: () -> Unit, model: LicenseViewModel = koinViewModel()) {
    val uiState by model.uiState
    LicenseScreenFrame(navUp, uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseScreenFrame(navUp: () -> Unit, uiState: LicenseUiState) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                navigationIcon = { AppBackButton(navUp) },
                title = { Text(stringResource(id = cc.chenhe.weargallery.R.string.about_licence)) },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
        ) {
            item {
                SegmentedColumn(title = stringResource(id = cc.chenhe.weargallery.R.string.about_licence)) {
                    uiState.licenses.forEach { license ->
                        item(key = license.name) { shape ->
                            val ctx = LocalContext.current
                            ActionRow(
                                title = license.name,
                                summary = "${license.author} · ${license.type}",
                                shape = shape,
                                onClick = { ctx.openInBrowser(license.url) },
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LicenseItem(license: LicenseViewModel.License) {
    val ctx = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                ctx.openInBrowser(license.url)
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            Text(text = license.name + "-" + license.author)
            CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
                Text(text = license.type)
                Text(text = license.url)
            }
        }
    }
}

private fun Context.openInBrowser(url: String) {
    try {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    } catch (_: Exception) {
    }
}