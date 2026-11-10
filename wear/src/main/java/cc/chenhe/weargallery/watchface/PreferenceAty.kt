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

package cc.chenhe.weargallery.watchface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import cc.chenhe.weargallery.ui.compose.WearGalleryTheme
import cc.chenhe.weargallery.ui.compose.WearRoute
import cc.chenhe.weargallery.ui.compose.screens.WatchFaceSettingsScreen
import cc.chenhe.weargallery.ui.compose.screens.WatchFaceTextScreen

/** Watch face configuration activity, opened from the system watch face picker or the app. */
class PreferenceAty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearGalleryTheme {
                AppScaffold(timeText = { TimeText() }) {
                    var textKind by rememberSaveable { mutableStateOf<String?>(null) }
                    BackHandler(enabled = textKind != null) { textKind = null }
                    when (val kind = textKind) {
                        null -> WatchFaceSettingsScreen(
                            onNavigate = { route ->
                                if (route is WearRoute.WatchFaceText) textKind = route.kind
                            }
                        )
                        else -> WatchFaceTextScreen(kind) { textKind = null }
                    }
                }
            }
        }
    }
}
