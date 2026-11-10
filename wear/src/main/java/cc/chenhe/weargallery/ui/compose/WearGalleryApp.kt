/**
 * Copyright (C) 2026 xxtvrxx233
 * Portions adapted from WearCompass (GPL-3.0), Copyright (C) 2026 xxtvrxx233.
 *
 * This file is distributed under the Wear Gallery Public License v1 (AGPL-3.0 with additional
 * terms). See the LICENSE file in the repository root.
 */

package cc.chenhe.weargallery.ui.compose

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.TimeText
import cc.chenhe.weargallery.ui.compose.screens.AboutScreen
import cc.chenhe.weargallery.ui.compose.screens.DisplaySettingsScreen
import cc.chenhe.weargallery.ui.compose.screens.LicenseDetailScreen
import cc.chenhe.weargallery.ui.compose.screens.LicensesScreen
import cc.chenhe.weargallery.ui.compose.screens.LocalDetailScreen
import cc.chenhe.weargallery.ui.compose.screens.MainPagerScreen
import cc.chenhe.weargallery.ui.compose.screens.MobileDetailScreen
import cc.chenhe.weargallery.ui.compose.screens.ThemeChoiceScreen
import cc.chenhe.weargallery.ui.compose.screens.ThemeSettingsScreen
import cc.chenhe.weargallery.ui.compose.screens.TransferSettingsScreen
import cc.chenhe.weargallery.ui.compose.screens.WatchFaceSettingsScreen
import cc.chenhe.weargallery.ui.compose.screens.WatchFaceTextScreen
import cc.chenhe.weargallery.ui.compose.screens.WebServerScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Root of the Wear OS UI. Navigation follows the Compass app: the main pager stays underneath and
 * destinations are rendered as overlay layers with Compass' dismiss animation, driven directly by
 * the predictive back gesture.
 */
@Composable
fun WearGalleryApp() {
    var overlayRoute by remember { mutableStateOf<WearRoute?>(null) }
    var subRoute by remember { mutableStateOf<WearRoute?>(null) }
    val overlaySlide = remember { Animatable(0f) }
    val subSlide = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val chromeVisible = remember { mutableStateOf(true) }

    fun open(route: WearRoute) {
        when {
            overlayRoute == null -> scope.launch {
                overlaySlide.snapTo(1f)
                overlayRoute = route
                overlaySlide.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(0.85f, Spring.StiffnessMedium),
                )
            }

            subRoute == null -> scope.launch {
                subSlide.snapTo(1f)
                subRoute = route
                subSlide.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(0.85f, Spring.StiffnessMedium),
                )
            }
        }
    }

    fun closeSub() {
        if (subRoute == null) return
        scope.launch {
            subSlide.animateTo(
                targetValue = 1f,
                animationSpec = spring(0.9f, Spring.StiffnessMediumLow),
            )
            subRoute = null
            subSlide.snapTo(0f)
        }
    }

    fun closeOverlay() {
        if (overlayRoute == null) return
        scope.launch {
            overlaySlide.animateTo(
                targetValue = 1f,
                animationSpec = spring(0.9f, Spring.StiffnessMediumLow),
            )
            overlayRoute = null
            overlaySlide.snapTo(0f)
        }
    }

    fun back() {
        if (subRoute != null) closeSub() else closeOverlay()
    }

    PredictiveBackHandler(enabled = subRoute != null) { progressFlow ->
        try {
            progressFlow.collect { event ->
                subSlide.snapTo(event.progress.coerceIn(0f, 1f))
            }
            subSlide.animateTo(1f, spring(0.9f, Spring.StiffnessMediumLow))
            subRoute = null
            subSlide.snapTo(0f)
        } catch (cancellation: CancellationException) {
            subSlide.animateTo(0f, spring(0.85f, Spring.StiffnessMedium))
            throw cancellation
        }
    }

    PredictiveBackHandler(enabled = subRoute == null && overlayRoute != null) { progressFlow ->
        try {
            progressFlow.collect { event ->
                overlaySlide.snapTo(event.progress.coerceIn(0f, 1f))
            }
            overlaySlide.animateTo(1f, spring(0.9f, Spring.StiffnessMediumLow))
            overlayRoute = null
            overlaySlide.snapTo(0f)
        } catch (cancellation: CancellationException) {
            overlaySlide.animateTo(0f, spring(0.85f, Spring.StiffnessMedium))
            throw cancellation
        }
    }

    CompositionLocalProvider(LocalDetailChromeState provides chromeVisible) {
        AppScaffold(
            timeText = { if (chromeVisible.value) TimeText() },
            containerColor = Color.Black,
        ) {
            BoxWithConstraints(Modifier.fillMaxSize().background(Color.Black)) {
                val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
                val isRound = LocalConfiguration.current.isScreenRound

                // Main pager stays underneath; it recedes while an overlay is open.
                Box(
                    Modifier
                        .fillMaxSize()
                        .coveredLayer {
                            if (overlayRoute == null) {
                                1f
                            } else if (subRoute != null) {
                                subSlide.value
                            } else {
                                overlaySlide.value
                            }
                        },
                ) {
                    MainPagerScreen(onNavigate = ::open)
                }

                if (overlayRoute != null || overlaySlide.isRunning) {
                    val top = subRoute != null
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .then(
                                if (top) {
                                    Modifier.coveredLayer { subSlide.value }
                                } else {
                                    Modifier.wearDismissLayer(
                                        progress = { overlaySlide.value },
                                        widthPx = widthPx,
                                        isRound = isRound,
                                    )
                                },
                            )
                            .clickable(interactionSource, indication = null) {},
                    ) {
                        overlayRoute?.let { route ->
                            RouteContent(route, onNavigate = ::open, onBack = ::back)
                        }
                    }
                }

                if (subRoute != null || subSlide.isRunning) {
                    val interactionSource = remember { MutableInteractionSource() }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .wearDismissLayer(
                                progress = { subSlide.value },
                                widthPx = widthPx,
                                isRound = isRound,
                            )
                            .clickable(interactionSource, indication = null) {},
                    ) {
                        subRoute?.let { route ->
                            RouteContent(route, onNavigate = ::open, onBack = ::back)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteContent(
    route: WearRoute,
    onNavigate: (WearRoute) -> Unit,
    onBack: () -> Unit,
) {
    when (route) {
        WearRoute.Main -> Unit
        is WearRoute.LocalDetail -> LocalDetailScreen(route, onBack = onBack)
        is WearRoute.MobileDetail -> MobileDetailScreen(route, onBack = onBack)
        WearRoute.WebServer -> WebServerScreen(onBack = onBack)
        WearRoute.DisplaySettings -> DisplaySettingsScreen(onBack = onBack)
        WearRoute.TransferSettings -> TransferSettingsScreen(onNavigate = onNavigate)
        WearRoute.WatchFaceSettings -> WatchFaceSettingsScreen(onNavigate = onNavigate)
        is WearRoute.WatchFaceText -> WatchFaceTextScreen(route.kind, onBack = onBack)
        WearRoute.Theme -> ThemeSettingsScreen(onNavigate = onNavigate, onBack = onBack)
        is WearRoute.ThemeChoice -> ThemeChoiceScreen(route.kind, onBack = onBack)
        WearRoute.About -> AboutScreen(onNavigate = onNavigate)
        WearRoute.Licenses -> LicensesScreen(
            onBack = onBack,
            onOpenLibrary = { onNavigate(WearRoute.LicenseDetail(it)) },
        )
        is WearRoute.LicenseDetail -> LicenseDetailScreen(route.index, onBack = onBack)
    }
}
