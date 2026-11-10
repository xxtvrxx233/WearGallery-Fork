/**
 * Copyright (C) 2026 xxtvrxx233
 * Portions adapted from WearCompass (GPL-3.0), Copyright (C) 2026 xxtvrxx233.
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

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import cc.chenhe.weargallery.ui.compose.components.WearPageHeader
import cc.chenhe.weargallery.ui.compose.components.WearPageList
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RotateLeft
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.bean.RemoteImage
import cc.chenhe.weargallery.common.bean.Image
import cc.chenhe.weargallery.db.RemoteImageDao
import cc.chenhe.weargallery.repository.ImageRepository
import cc.chenhe.weargallery.repository.RemoteImageRepository
import cc.chenhe.weargallery.ui.compose.WearRoute
import cc.chenhe.weargallery.ui.compose.coveredLayer
import cc.chenhe.weargallery.ui.compose.wearDismissLayer
import cc.chenhe.weargallery.ui.compose.components.RemotePreviewImage
import cc.chenhe.weargallery.ui.imagedetail.ImageDetailBaseViewModel
import cc.chenhe.weargallery.ui.imagedetail.local.LocalImageDetailSource
import cc.chenhe.weargallery.ui.imagedetail.local.LocalImageDetailViewModel
import cc.chenhe.weargallery.ui.imagedetail.mobile.MobileImageDetailViewModel
import cc.chenhe.weargallery.ui.main.SharedViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@OptIn(androidx.paging.ExperimentalPagingApi::class)
@Composable
fun LocalDetailScreen(route: WearRoute.LocalDetail, onBack: () -> Unit) {
    val sharedModel: SharedViewModel = koinViewModel()
    val source = remember(route.source) {
        runCatching { LocalImageDetailSource.valueOf(route.source) }
            .getOrDefault(LocalImageDetailSource.IMAGES)
    }
    val model: LocalImageDetailViewModel = koinViewModel { parametersOf(source, route.bucketId) }
    val items = model.pagingImages.collectAsLazyPagingItems()

    val context = LocalContext.current
    val repository: RemoteImageRepository = koinInject()
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<ImageRepository.Pending?>(null) }
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        pendingDelete = null
    }

    val initialPage = remember {
        if (source == LocalImageDetailSource.IMAGES) sharedModel.currentPosition else 0
    }

    DetailPager(
        itemCount = items.itemCount,
        initialPage = initialPage,
        loadState = items.loadState.refresh,
        model = model,
        hasLocalFile = { true },
        canLoadHd = { false },
        onRetry = { items.retry() },
        onPageSelected = { index ->
            sharedModel.currentPosition = index
            model.setCurrentItem(index, items.peek(index))
        },
        onDelete = { index ->
            val uri = items.peek(index)?.uri
            if (uri != null) {
                scope.launch {
                    val pending = runCatching {
                        repository.deleteLocalImage(context, listOf(uri))
                    }.getOrNull()
                    if (pending != null) {
                        pendingDelete = pending
                        deleteLauncher.launch(
                            IntentSenderRequest.Builder(pending.intentSender).build()
                        )
                    }
                }
            }
        },
        onLoadHd = null,
        pageContent = { index, modifier ->
            DetailImage(
                model = items.peek(index)?.uri,
                contentDescription = items.peek(index)?.name,
                modifier = modifier,
            )
        },
    )
}

@OptIn(androidx.paging.ExperimentalPagingApi::class)
@Composable
fun MobileDetailScreen(route: WearRoute.MobileDetail, onBack: () -> Unit) {
    val model: MobileImageDetailViewModel = koinViewModel { parametersOf(route.bucketId) }
    val items = model.pagingImages.collectAsLazyPagingItems()
    val remoteImageDao: RemoteImageDao = koinInject()
    var pendingUris by remember { mutableStateOf<Collection<Uri>?>(null) }
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingUris?.let { uris ->
                ProcessLifecycleOwner.get().lifecycleScope.launch {
                    remoteImageDao.clearLocalUri(uris)
                }
            }
        }
    }
    val deleteRequest by model.deleteRequestEvent.observeAsState()
    LaunchedEffect(deleteRequest) {
        deleteRequest?.let {
            pendingUris = it.uris
            deleteLauncher.launch(IntentSenderRequest.Builder(it.intentSender).build())
        }
    }

    DetailPager(
        itemCount = items.itemCount,
        initialPage = 0,
        loadState = items.loadState.refresh,
        model = model,
        hasLocalFile = { index -> items.peek(index)?.localUri != null },
        canLoadHd = { index -> items.peek(index)?.localUri == null },
        onRetry = { items.retry() },
        onPageSelected = { index -> model.setCurrentItem(index, items.peek(index)) },
        onDelete = { index -> items.peek(index)?.let { model.deleteHd(it) } },
        onLoadHd = { index -> items.peek(index)?.let { model.loadHd(it) } },
        pageContent = { index, modifier ->
            val item = items.peek(index)
            val localUri = item?.localUri
            if (localUri != null) {
                DetailImage(
                    model = localUri,
                    contentDescription = item.name,
                    modifier = modifier,
                )
            } else {
                RemotePreviewImage(
                    remoteUri = item?.uri,
                    contentDescription = item?.name,
                    contentScale = ContentScale.Fit,
                    modifier = modifier,
                )
            }
        },
    )
}

@Composable
private fun <T : Any> DetailPager(
    itemCount: Int,
    initialPage: Int,
    loadState: LoadState,
    model: ImageDetailBaseViewModel<T>,
    hasLocalFile: (Int) -> Boolean,
    canLoadHd: (Int) -> Boolean,
    onRetry: () -> Unit,
    onPageSelected: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onLoadHd: ((Int) -> Unit)?,
    pageContent: @Composable (Int, Modifier) -> Unit,
) {
    val keepScreenOn by model.keepScreenOn.observeAsState(false)
    val view = LocalView.current
    LaunchedEffect(keepScreenOn) { view.keepScreenOn = keepScreenOn }

    if (itemCount == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (loadState) {
                is LoadState.Loading -> CircularProgressIndicator()
                is LoadState.Error -> Text(stringResource(R.string.empty))
                else -> Text(stringResource(R.string.local_empty))
            }
        }
        if (loadState is LoadState.Error) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.retry))
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, (itemCount - 1).coerceAtLeast(0)),
        pageCount = { itemCount },
    )
    var jumped by remember { mutableStateOf(false) }
    LaunchedEffect(itemCount) {
        if (!jumped && itemCount > 0) {
            jumped = true
            val target = initialPage.coerceIn(0, itemCount - 1)
            if (pagerState.currentPage != target) pagerState.scrollToPage(target)
            onPageSelected(target)
        }
    }
    LaunchedEffect(pagerState.currentPage) { onPageSelected(pagerState.currentPage) }

    // Chrome (time text + zoom buttons) hides after a short idle period, as the old detail page did.
    val chromeState = cc.chenhe.weargallery.ui.compose.LocalDetailChromeState.current
    var chromeVisible by remember { mutableStateOf(true) }
    var interactions by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    fun pokeChrome() {
        chromeVisible = true
        interactions++
    }
    LaunchedEffect(interactions, chromeVisible) {
        if (chromeVisible) {
            kotlinx.coroutines.delay(3000)
            chromeVisible = false
        }
    }
    LaunchedEffect(chromeVisible) { chromeState.value = chromeVisible }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { chromeState.value = true }
    }

    // Zoom/pan/rotation live in plain state objects that are only read inside graphicsLayer,
    // so a gesture updates the draw layer instead of recomposing the page.
    val zoomStates = remember { mutableStateMapOf<Int, ZoomState>() }
    val scope = rememberCoroutineScope()
    // 1 = fully hidden, 0 = fully shown; copied from Compass' overlay animation.
    val menuProgress = remember { Animatable(1f) }
    var menuPresent by remember { mutableStateOf(false) }
    val isRound = androidx.compose.ui.platform.LocalConfiguration.current.isScreenRound

    fun openMenu() {
        if (menuPresent) return
        scope.launch {
            menuProgress.snapTo(1f)
            chromeVisible = false
            menuPresent = true
            menuProgress.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.85f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMedium,
                ),
            )
        }
    }

    fun closeMenu() {
        if (!menuPresent) return
        scope.launch {
            menuProgress.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.9f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            )
            menuPresent = false
        }
    }

    // Back on the menu returns to the picture instead of leaving the detail page.
    BackHandler(enabled = menuPresent) { closeMenu() }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var dragTotal = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onDragEnd = {
                        val width = size.width.toFloat()
                        when {
                            dragTotal < -width * 0.15f -> openMenu()
                            dragTotal > width * 0.15f -> closeMenu()
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { change, amount ->
                        dragTotal += amount
                        change.consume()
                    },
                )
            },
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val currentZoom = zoomStates[pagerState.currentPage]

        VerticalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .coveredLayer { menuProgress.value },
        ) { page ->
            val zoom = remember(page) {
                ZoomState(scope).also { zoomStates[page] = it }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Reads happen in the draw phase: gestures do not recompose the page.
                        rotationZ = zoom.rotation.floatValue
                        scaleX = zoom.scale.floatValue
                        scaleY = zoom.scale.floatValue
                        translationX = zoom.offsetX.floatValue
                        translationY = zoom.offsetY.floatValue
                    }
                    .pointerInput(page) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val zoomChange = event.calculateZoom()
                                val pan = event.calculatePan()
                                val multiTouch = event.changes.count { it.pressed } > 1
                                val current = zoom.scale.floatValue
                                if ((multiTouch || current > 1f) &&
                                    (zoomChange != 1f || pan != Offset.Zero)
                                ) {
                                    zoom.applyGesture(zoomChange, pan)
                                    event.changes.forEach { it.consume() }
                                }
                                if (event.changes.none { it.pressed }) break
                            } while (true)
                        }
                    }
                    .pointerInput(page) {
                        detectTapGestures(
                            onTap = {
                                if (chromeVisible) chromeVisible = false else pokeChrome()
                            },
                            onDoubleTap = { zoom.toggleDoubleTap() },
                        )
                    },
            ) {
                pageContent(page, Modifier.fillMaxSize())
            }
        }

        // Zoom buttons stay on the picture page so their effect is visible immediately.
        if (!menuPresent && chromeVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ZoomButton(Icons.Rounded.ZoomOut, stringResource(R.string.image_detail_zoom_out)) {
                    pokeChrome()
                    currentZoom?.zoomOut()
                }
                ZoomButton(Icons.Rounded.ZoomIn, stringResource(R.string.image_detail_zoom_in)) {
                    pokeChrome()
                    currentZoom?.zoomIn()
                }
            }
        }

        if (menuPresent) {
            DetailMenu(
                modifier = Modifier.wearDismissLayer({ menuProgress.value }, widthPx, isRound),
                canLoadHd = canLoadHd(pagerState.currentPage),
                canDelete = hasLocalFile(pagerState.currentPage),
                onRotateLeft = {
                    currentZoom?.rotateBy(-90f)
                    closeMenu()
                },
                onRotateRight = {
                    currentZoom?.rotateBy(90f)
                    closeMenu()
                },
                onLoadHd = onLoadHd?.let {
                    {
                        it(pagerState.currentPage)
                        closeMenu()
                    }
                },
                onDelete = {
                    onDelete(pagerState.currentPage)
                    closeMenu()
                },
                onClose = { closeMenu() },
            )
        }
    }
}

/** Per-page zoom, pan and rotation, read only from graphicsLayer/draw code. */
private const val PanSensitivity = 1.8f

private class ZoomState(private val scope: CoroutineScope) {
    val scale = androidx.compose.runtime.mutableFloatStateOf(1f)
    val offsetX = androidx.compose.runtime.mutableFloatStateOf(0f)
    val offsetY = androidx.compose.runtime.mutableFloatStateOf(0f)
    val rotation = androidx.compose.runtime.mutableFloatStateOf(0f)

    private var scaleJob: kotlinx.coroutines.Job? = null
    private var offsetJob: kotlinx.coroutines.Job? = null
    private var rotationJob: kotlinx.coroutines.Job? = null

    /** Gesture updates snap immediately; running animations are cancelled. */
    fun applyGesture(zoomChange: Float, pan: Offset) {
        scaleJob?.cancel()
        offsetJob?.cancel()
        val newScale = (scale.floatValue * zoomChange).coerceIn(1f, 5f)
        scale.floatValue = newScale
        if (newScale > 1f) {
            offsetX.floatValue += pan.x * PanSensitivity
            offsetY.floatValue += pan.y * PanSensitivity
        } else {
            offsetX.floatValue = 0f
            offsetY.floatValue = 0f
        }
    }

    private fun animateScale(target: Float) {
        scaleJob?.cancel()
        offsetJob?.cancel()
        scaleJob = scope.launch {
            androidx.compose.animation.core.animate(
                initialValue = scale.floatValue,
                targetValue = target,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.75f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            ) { value, _ -> scale.floatValue = value }
            if (target <= 1f) resetOffsets()
        }
    }

    private fun resetOffsets() {
        val startX = offsetX.floatValue
        val startY = offsetY.floatValue
        offsetJob = scope.launch {
            androidx.compose.animation.core.animate(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.8f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            ) { value, _ ->
                offsetX.floatValue = startX * value
                offsetY.floatValue = startY * value
            }
        }
    }

    fun zoomIn() = animateScale((scale.floatValue * 1.5f).coerceAtMost(5f))

    fun zoomOut() = animateScale((scale.floatValue / 1.5f).coerceAtLeast(1f))

    fun toggleDoubleTap() = animateScale(if (scale.floatValue > 1f) 1f else 2.5f)

    fun rotateBy(delta: Float) {
        rotationJob?.cancel()
        rotationJob = scope.launch {
            androidx.compose.animation.core.animate(
                initialValue = rotation.floatValue,
                targetValue = rotation.floatValue + delta,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.8f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            ) { value, _ -> rotation.floatValue = value }
        }
    }
}

/**
 * Decodes the picture at a resolution well above the 384px screen, so zooming enlarges real
 * pixels instead of upscaling a screen-sized bitmap.
 */
@Composable
private fun DetailImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AsyncImage(
        model = coil.request.ImageRequest.Builder(context)
            .data(model)
            .size(1280)
            .scale(coil.size.Scale.FIT)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

@Composable
private fun ZoomButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(36.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(),
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp))
    }
}



/** Operation panel styled like the BakaSU Wear manager panels, without its pull gesture. */
@Composable
private fun DetailMenu(
    modifier: Modifier = Modifier,
    canLoadHd: Boolean,
    canDelete: Boolean,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onLoadHd: (() -> Unit)?,
    onDelete: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        WearPageList(
            edgeButton = {
                EdgeButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel),
                    )
                }
            },
        ) { spec ->
            item { WearPageHeader(spec, stringResource(R.string.image_operation_title)) }
            item {
                MenuRow(
                    spec,
                    Icons.Rounded.RotateLeft,
                    stringResource(R.string.image_operation_rotate_left),
                    onRotateLeft,
                )
            }
            item {
                MenuRow(
                    spec,
                    Icons.Rounded.RotateRight,
                    stringResource(R.string.image_operation_rotate_right),
                    onRotateRight,
                )
            }
            if (onLoadHd != null) {
                item {
                    MenuRow(
                        spec,
                        Icons.Rounded.Download,
                        stringResource(R.string.image_operation_hd),
                        onLoadHd,
                        enabled = canLoadHd,
                    )
                }
            }
            item {
                MenuRow(
                    spec,
                    Icons.Rounded.Delete,
                    stringResource(R.string.image_operation_delete),
                    onDelete,
                    enabled = canDelete,
                )
            }
        }
    }
}

@Composable
private fun TransformingLazyColumnItemScope.MenuRow(
    spec: TransformationSpec,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
        transformation = SurfaceTransformation(spec),
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
    )
}
