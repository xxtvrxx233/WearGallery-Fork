/**
 * Copyright (C) 2026 xxtvrxx233
 *
 * This file is distributed under the Wear Gallery Public License v1 (AGPL-3.0 with additional
 * terms). See the LICENSE file in the repository root.
 */

package cc.chenhe.weargallery.ui.compose

/**
 * Overlay destinations. [Main] is the pager of the top-level pages; every other route opens as a
 * Compass-style overlay above it, and at most one sub-overlay can sit on top of an overlay.
 */
sealed interface WearRoute {

    data object Main : WearRoute

    /** Detail of the local gallery. [source] is the name of `LocalImageDetailSource`. */
    data class LocalDetail(
        val source: String,
        val bucketId: Int,
        val totalCount: Int,
    ) : WearRoute

    data class MobileDetail(
        val bucketId: Int,
        val totalCount: Int,
    ) : WearRoute

    data object WebServer : WearRoute

    data object DisplaySettings : WearRoute

    data object TransferSettings : WearRoute

    data object WatchFaceSettings : WearRoute

    /** Digital watch face text style pages. [kind] is one of `position`, `format`, `color`. */
    data class WatchFaceText(val kind: String) : WearRoute

    data object Theme : WearRoute

    /** Theme palette style / color spec choice pages. [kind] is `palette` or `spec`. */
    data class ThemeChoice(val kind: String) : WearRoute

    data object About : WearRoute

    data object Licenses : WearRoute

    data class LicenseDetail(val index: Int) : WearRoute
}
