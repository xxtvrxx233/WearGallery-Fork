/**
 * Copyright (C) 2026 xxtvrxx233
 *
 * Adapted from PortromOTAUpdater, Copyright (C) 2026 xxtvrxx233; relicensed for this project
 * by its sole author.
 *
 * This file is distributed under the Wear Gallery Public License v1 (AGPL-3.0 with additional
 * terms). See the LICENSE file in the repository root.
 */

package cc.chenhe.weargallery.ui.compose

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether the detail screen currently shows its chrome (the system time text and the zoom
 * buttons). Detail screens flip it off after a few seconds of inactivity and on tap.
 */
val LocalDetailChromeVisible: MutableState<Boolean> = mutableStateOf(true)

/** Provided by [WearGalleryApp] so the scaffold time text follows the detail chrome. */
val LocalDetailChromeState = staticCompositionLocalOf<MutableState<Boolean>> {
    LocalDetailChromeVisible
}
