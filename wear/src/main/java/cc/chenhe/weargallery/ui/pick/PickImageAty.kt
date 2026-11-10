/**
 * Copyright (C) 2020 Chenhe
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

package cc.chenhe.weargallery.ui.pick

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import cc.chenhe.weargallery.ui.compose.WearGalleryTheme
import cc.chenhe.weargallery.ui.compose.screens.PickImageScreen

/**
 * An activity that lets users choose local picture. The default result is [Activity.RESULT_CANCELED]. If the user
 * selects a picture then the result is [Activity.RESULT_OK] and the uri of the selected picture will be saved in the
 * result intent, you should use [Intent.getData] to get it.
 */
class PickImageAty : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContent {
            WearGalleryTheme {
                AppScaffold(timeText = { TimeText() }) {
                    PickImageScreen(onPick = { image ->
                        setResult(Activity.RESULT_OK, Intent().setData(image.uri))
                        finish()
                    })
                }
            }
        }
    }
}
