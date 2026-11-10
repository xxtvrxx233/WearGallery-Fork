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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Filled circular back affordance used by the MD3E top app bars. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppBackButton(onClick: () -> Unit) {
    Row {
        IconButton(
            onClick = onClick,
            modifier = Modifier.padding(start = 12.dp).size(36.dp),
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.size(10.dp))
    }
}
