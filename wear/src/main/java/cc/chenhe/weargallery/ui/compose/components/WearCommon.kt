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

package cc.chenhe.weargallery.ui.compose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.platform.LocalConfiguration
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

/** Extra bottom room so the last item clears the curved edge of round screens. */
internal val ListExtraBottomPadding = 64.dp

/**
 * Standard scrollable page: a [ScreenScaffold] plus a [TransformingLazyColumn] with the Wear M3
 * scroll transformation applied to every row.
 */
@Composable
fun WearPageList(
    listState: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
    edgeButton: @Composable BoxScope.() -> Unit = {},
    content: TransformingLazyColumnScope.(TransformationSpec) -> Unit,
) {
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = listState, edgeButton = edgeButton) { contentPadding ->
        val layoutDirection = LocalLayoutDirection.current
        TransformingLazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + ListExtraBottomPadding,
            ),
        ) {
            content(transformationSpec)
        }
    }
}

/**
 * Page title following the Wear M3 list header guidance: centered, with horizontal padding scaled
 * for round screens so the text never hugs the curved edge.
 */
@Composable
fun TransformingLazyColumnItemScope.WearPageHeader(
    transformationSpec: TransformationSpec,
    text: String,
) {
    val side = maxOf(14.dp, (LocalConfiguration.current.screenWidthDp * 0.073f).dp)
    val defaults = ListHeaderDefaults.ContentPadding
    ListHeader(
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        contentPadding = PaddingValues(
            start = side,
            end = side,
            top = defaults.calculateTopPadding(),
            bottom = defaults.calculateBottomPadding(),
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

/** A full-width tappable row used across the settings and gallery lists. */
@Composable
fun TransformingLazyColumnItemScope.WearActionRow(
    transformationSpec: TransformationSpec,
    label: String,
    icon: ImageVector? = null,
    secondaryLabel: String? = null,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        icon = icon?.let { { Icon(it, contentDescription = null) } } ?: {},
        label = { Text(label, maxLines = 2) },
        secondaryLabel = secondaryLabel?.let {
            { Text(it, maxLines = 3, textAlign = TextAlign.Start) }
        },
    )
}

/** Centered progress indicator used while a page has no data yet. */
@Composable
fun WearLoadingScreen(modifier: Modifier = Modifier) {
    ScreenScaffold {
        androidx.compose.foundation.layout.Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

/** Full page message with an optional action (empty or error state). */
@Composable
fun WearMessageScreen(
    title: String,
    message: String? = null,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    WearPageList { spec ->
        item {
            ColumnHeaderSpacer(spec, icon)
        }
        item {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        message?.let {
            item {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            item {
                Button(
                    onClick = onAction,
                    label = { Text(actionLabel) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TransformingLazyColumnItemScope.ColumnHeaderSpacer(
    spec: TransformationSpec,
    icon: ImageVector?,
) {
    if (icon != null) {
        ListHeader(transformation = SurfaceTransformation(spec)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Wear-styled confirmation dialog. */
@Composable
fun WearConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        visible = true,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            IconButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = IconButtonDefaults.filledIconButtonColors(),
            ) {
                Icon(Icons.Rounded.Check, contentDescription = confirmLabel)
            }
        },
        dismissButton = {
            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.filledTonalIconButtonColors(),
            ) {
                Icon(Icons.Rounded.Close, contentDescription = dismissLabel)
            }
        },
        onDismissRequest = onDismiss,
    )
}

/** Small vertical spacing item. */
@Composable
fun TransformingLazyColumnItemScope.WearSpacer(height: androidx.compose.ui.unit.Dp = 4.dp) {
    Spacer(Modifier.fillMaxWidth().height(height))
}
