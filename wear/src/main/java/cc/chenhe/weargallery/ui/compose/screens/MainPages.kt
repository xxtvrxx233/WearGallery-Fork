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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Watch
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CardDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.HorizontalPagerScaffold
import androidx.wear.compose.material3.PagerScaffoldDefaults
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.bean.RemoteImageFolder
import cc.chenhe.weargallery.common.bean.Error
import cc.chenhe.weargallery.common.bean.Image
import cc.chenhe.weargallery.common.bean.ImageFolder
import cc.chenhe.weargallery.common.bean.Loading
import cc.chenhe.weargallery.common.bean.Resource
import cc.chenhe.weargallery.common.bean.Success
import cc.chenhe.weargallery.common.util.getVersionName
import cc.chenhe.weargallery.ui.compose.WearRoute
import cc.chenhe.weargallery.ui.compose.components.WearActionRow
import cc.chenhe.weargallery.ui.compose.components.WearConfirmDialog
import cc.chenhe.weargallery.ui.compose.components.WearLoadingScreen
import cc.chenhe.weargallery.ui.compose.components.WearMessageScreen
import cc.chenhe.weargallery.ui.compose.components.WearPageHeader
import cc.chenhe.weargallery.ui.compose.components.RemotePreviewImage
import cc.chenhe.weargallery.ui.compose.components.WearPageList
import cc.chenhe.weargallery.ui.local.LocalImagesViewModel
import cc.chenhe.weargallery.ui.main.PageViewModel
import cc.chenhe.weargallery.ui.main.SharedViewModel
import cc.chenhe.weargallery.uilts.showPhoneImages
import coil.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

/** The vertical pager hosting the top-level pages. */
@Composable
fun MainPagerScreen(onNavigate: (WearRoute) -> Unit) {
    val pageModel: PageViewModel = koinViewModel()
    val items by pageModel.items.observeAsState(emptyList())
    if (items.isEmpty()) {
        WearLoadingScreen()
        return
    }
    val pagerState = rememberPagerState(pageCount = { items.size })
    HorizontalPagerScaffold(
        pagerState = pagerState,
        pageIndicator = {
            Box(Modifier.padding(bottom = 6.dp)) { HorizontalPageIndicator(pagerState) }
        },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = PagerScaffoldDefaults.snapWithSpringFlingBehavior(pagerState),
        ) { index ->
            when (items.getOrNull(index)) {
                PageViewModel.Item.Local -> LocalImagesPage(onNavigate)
                PageViewModel.Item.Phone -> MobileImagesPage(onNavigate)
                PageViewModel.Item.Explore -> ExplorePage(onNavigate)
                PageViewModel.Item.WhetherShowPhone -> ShowPhonePromptPage()
                null -> Unit
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Local gallery
// ---------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalImagesPage(onNavigate: (WearRoute) -> Unit) {
    val model: LocalImagesViewModel = koinViewModel()
    val sharedModel: SharedViewModel = koinViewModel()
    val folderMode by model.folderMode.observeAsState(false)
    val folders by model.localFolders.observeAsState(Loading(null))
    val images by sharedModel.localImages.observeAsState()

    var selectionMode by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<Any>() }
    var confirmDelete by remember { mutableStateOf(false) }

    fun toggle(item: Any) {
        if (selected.remove(item)) {
            if (selected.isEmpty()) selectionMode = false
        } else {
            selected.add(item)
        }
        model.setSelectionMode(selectionMode)
    }

    val folderList = folders?.data
    val imageList = images?.data

    // Back leaves selection mode before it leaves the page.
    androidx.activity.compose.BackHandler(enabled = selectionMode) {
        selectionMode = false
        selected.clear()
    }

    val listState = androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState()
    val showViewToggle by androidx.compose.runtime.derivedStateOf {
        !listState.canScrollForward
    }

    Box(Modifier.fillMaxSize()) {
    WearPageList(listState = listState) { spec ->
        item {
            WearPageHeader(
                spec,
                if (selectionMode) {
                    stringResource(R.string.local_title_selection, selected.size)
                } else {
                    stringResource(R.string.drawer_local_gallery)
                },
            )
        }

        if (folderMode) {
            val list = folderList
            if (list == null) {
                item { Text(stringResource(R.string.image_detail_load_ing)) }
            } else if (list.isEmpty()) {
                item { Text(stringResource(R.string.local_empty)) }
            } else {
                list.forEach { folder ->
                    item(key = "folder-${folder.id}") {
                        FolderRow(
                            spec = spec,
                            folder = folder,
                            checked = selected.contains(folder),
                            selectionMode = selectionMode,
                            onClick = {
                                if (selectionMode) {
                                    toggle(folder)
                                } else {
                                    onNavigate(
                                        WearRoute.LocalDetail(
                                            source = "FOLDER",
                                            bucketId = folder.id,
                                            totalCount = folder.imgNum,
                                        )
                                    )
                                }
                            },
                            onLongClick = {
                                selectionMode = true
                                toggle(folder)
                            },
                        )
                    }
                }
            }
        } else {
            val list = imageList
            if (list == null) {
                item { Text(stringResource(R.string.image_detail_load_ing)) }
            } else if (list.isEmpty()) {
                item { Text(stringResource(R.string.local_empty)) }
            } else {
                list.chunked(2).forEachIndexed { rowIndex, row ->
                    item(key = "row-$rowIndex") {
                        Row(
                            modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEachIndexed { column, image ->
                                val position = rowIndex * 2 + column
                                ImageCell(
                                    image = image,
                                    checked = selected.contains(image),
                                    selectionMode = selectionMode,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (selectionMode) {
                                            toggle(image)
                                        } else {
                                            sharedModel.currentPosition = position
                                            onNavigate(
                                                WearRoute.LocalDetail(
                                                    source = "IMAGES",
                                                    bucketId = -1,
                                                    totalCount = list.size,
                                                )
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        selectionMode = true
                                        toggle(image)
                                    },
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
        androidx.compose.animation.AnimatedVisibility(
            visible = selectionMode || showViewToggle,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
        ) {
                IconButton(
                    onClick = {
                        if (selectionMode) {
                            confirmDelete = true
                        } else {
                            model.toggleListMode()
                        }
                    },
                    enabled = !selectionMode || selected.isNotEmpty(),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(
                        imageVector = when {
                            selectionMode -> Icons.Rounded.Delete
                            folderMode -> Icons.Rounded.GridView
                            else -> Icons.Rounded.ViewList
                        },
                        contentDescription = if (selectionMode) {
                            stringResource(R.string.image_operation_delete)
                        } else {
                            stringResource(R.string.preference_display)
                        },
                    )
                }
            }
        }

    if (confirmDelete) {
        val isFolder = selected.firstOrNull() is ImageFolder
        val count = selected.size
        WearConfirmDialog(
            title = stringResource(R.string.confirm),
            message = if (isFolder) {
                pluralStringResource(R.plurals.local_delete_image_folders_confirm, count, count)
            } else {
                pluralStringResource(R.plurals.local_delete_images_confirm, count, count)
            },
            confirmLabel = stringResource(R.string.confirm),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                if (isFolder) {
                    sharedModel.deleteLocalImageFolders(selected.filterIsInstance<ImageFolder>())
                } else {
                    sharedModel.deleteLocalImages(selected.filterIsInstance<Image>().map { it.uri })
                }
                selected.clear()
                selectionMode = false
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransformingLazyColumnItemScope.FolderRow(
    spec: TransformationSpec,
    folder: ImageFolder,
    checked: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, spec)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        transformation = SurfaceTransformation(spec),
        colors = if (checked) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = folder.preview.uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(folder.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = folder.imgNum.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageCell(
    image: Image,
    checked: Boolean,
    selectionMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(modifier = modifier.aspectRatio(1f)) {
        AsyncImage(
            model = image.uri,
            contentDescription = image.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        )
        if (selectionMode) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(
                    if (checked) R.drawable.ic_check_on else R.drawable.ic_check_off
                ),
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Phone gallery
// ---------------------------------------------------------------------------

@Composable
fun MobileImagesPage(onNavigate: (WearRoute) -> Unit) {
    val sharedModel: SharedViewModel = koinViewModel()
    val remoteFolders by sharedModel.remoteFolders.observeAsState(Loading(null))
    val conflict by sharedModel.isVersionConflict.observeAsState()
    var showHelp by remember { mutableStateOf(false) }
    var showConflict by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(conflict) {
        if (conflict == true) showConflict = true
    }

    val folders = remoteFolders?.data

    if (folders == null && remoteFolders is Loading) {
        WearLoadingScreen()
        return
    }

    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.drawer_phone_gallery)) }

        when {
            folders == null -> item {
                Text(
                    text = stringResource(R.string.image_detail_load_ing),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            folders.isEmpty() && remoteFolders is Error -> {
                item {
                    WearActionRow(
                        spec,
                        label = stringResource(R.string.retry),
                        onClick = { sharedModel.retryFetchRemoteImageFolders() },
                    )
                }
                item {
                    WearActionRow(
                        spec,
                        label = stringResource(R.string.mobile_load_gallery_err_tip),
                        onClick = {
                            sharedModel.checkMobileVersion()
                            showHelp = true
                        },
                    )
                }
            }

            folders.isEmpty() -> item {
                Text(
                    text = stringResource(R.string.mobile_empty),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            else -> folders.forEach { folder ->
                item(key = "remote-${folder.bucketId}") {
                    RemoteFolderRow(
                        spec = spec,
                        folder = folder,
                        onClick = {
                            sharedModel.checkMobileVersion()
                            onNavigate(
                                WearRoute.MobileDetail(
                                    bucketId = folder.bucketId,
                                    totalCount = folder.imageCount,
                                )
                            )
                        },
                    )
                }
            }
        }
    }

    if (showHelp) {
        HelpDialog(
            title = stringResource(R.string.mobile_load_gallery_err_tip),
            message = stringResource(R.string.mobile_load_gallery_err_content),
            onDismiss = { showHelp = false },
        )
    }

    if (showConflict) {
        val context = androidx.compose.ui.platform.LocalContext.current
        HelpDialog(
            title = stringResource(R.string.mobile_load_gallery_err_tip),
            message = stringResource(
                R.string.mobile_version_conflict_content,
                getVersionName(context),
                sharedModel.mobileVersion ?: "?",
            ),
            onDismiss = {
                showConflict = false
                sharedModel.retryFetchRemoteImageFolders()
            },
        )
    }
}

@Composable
private fun TransformingLazyColumnItemScope.RemoteFolderRow(
    spec: TransformationSpec,
    folder: RemoteImageFolder,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
        transformation = SurfaceTransformation(spec),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RemotePreviewImage(
                remoteUri = folder.previewUri,
                contentDescription = folder.bucketName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(folder.bucketName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = folder.imageCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HelpDialog(title: String, message: String, onDismiss: () -> Unit) {
    androidx.wear.compose.material3.AlertDialog(
        visible = true,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.confirm))
            }
        },
        onDismissRequest = onDismiss,
    )
}

// ---------------------------------------------------------------------------
// Explore / settings root
// ---------------------------------------------------------------------------

@Composable
fun ExplorePage(onNavigate: (WearRoute) -> Unit) {
    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.drawer_explore)) }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.preference_display),
                icon = Icons.Rounded.Visibility,
                onClick = { onNavigate(WearRoute.DisplaySettings) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.preference_transfer),
                icon = Icons.Rounded.Sync,
                onClick = { onNavigate(WearRoute.TransferSettings) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.wf_preference_title),
                icon = Icons.Rounded.Watch,
                onClick = { onNavigate(WearRoute.WatchFaceSettings) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.preference_theme),
                icon = Icons.Rounded.Palette,
                onClick = { onNavigate(WearRoute.Theme) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.preference_about),
                icon = Icons.Rounded.Info,
                onClick = { onNavigate(WearRoute.About) },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Prompt when phone images are queried but hidden
// ---------------------------------------------------------------------------

@Composable
fun ShowPhonePromptPage() {
    val context = androidx.compose.ui.platform.LocalContext.current
    WearPageList { spec ->
        item { WearPageHeader(spec, stringResource(R.string.fr_whether_show_phone_image_title)) }
        item {
            Text(
                text = stringResource(R.string.fr_whether_show_phone_image_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.fr_whether_show_phone_image_enable),
                icon = Icons.Rounded.Check,
                onClick = { showPhoneImages(context, shouldQuery = false, show = true) },
            )
        }
        item {
            WearActionRow(
                spec,
                label = stringResource(R.string.fr_whether_show_phone_image_disable),
                icon = Icons.Rounded.Close,
                onClick = { showPhoneImages(context, shouldQuery = false, show = false) },
            )
        }
    }
}


/** Round, filled icon button used in the ScreenScaffold edge slot. */
@Composable
private fun EdgeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(24.dp))
    }
}
