/**
 * Copyright (C) 2026 xxtvrxx233
 *
 * Adapted from PortromOTAUpdater, Copyright (C) 2026 xxtvrxx233; relicensed for this project
 * by its sole author.
 *
 * This file is distributed under the Wear Gallery Public License v1 (AGPL-3.0 with additional
 * terms). See the LICENSE file in the repository root.
 */

package cc.chenhe.weargallery.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close

private val RowContainerColor
    @Composable get() = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

/** A settings row with a switch, styled like the segmented cards in OTA2. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreferenceRow(
    title: String,
    summary: String = "",
    icon: ImageVector? = null,
    checked: Boolean,
    enabled: Boolean = true,
    shape: Shape,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }
    val summaryColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }

    Surface(shape = shape, color = RowContainerColor, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    if (!checked) haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    onCheckedChange(!checked)
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.also {
                Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
                if (summary.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(summary, style = MaterialTheme.typography.bodyMedium, color = summaryColor)
                }
            }
            Spacer(Modifier.width(16.dp))
            Switch(
                enabled = enabled,
                checked = checked,
                onCheckedChange = null,
                thumbContent = {
                    Icon(
                        imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
            )
        }
    }
}

/** A settings row that opens another page and may host a trailing composable. */
@Composable
fun ActionRow(
    title: String,
    summary: String = "",
    icon: ImageVector? = null,
    shape: Shape,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }
    val summaryColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    }

    Surface(shape = shape, color = RowContainerColor, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon?.also {
                Icon(it, contentDescription = null, tint = contentColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
                if (summary.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(summary, style = MaterialTheme.typography.bodyMedium, color = summaryColor)
                }
            }
            trailing?.also {
                Spacer(Modifier.width(16.dp))
                it()
            }
        }
    }
}
