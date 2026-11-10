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

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import cc.chenhe.weargallery.repository.RemoteImageRepository
import coil.compose.AsyncImage
import org.koin.compose.koinInject

/**
 * Loads a picture from the phone. The repository downloads it into the local preview cache and
 * returns a file uri, which is what the watch can actually display.
 */
@Composable
fun RemotePreviewImage(
    remoteUri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val repository: RemoteImageRepository = koinInject()
    val localUri by produceState<Uri?>(initialValue = null, remoteUri) {
        value = if (remoteUri == null) {
            null
        } else {
            runCatching { repository.loadImagePreview(context, remoteUri) }.getOrNull()
        }
    }
    AsyncImage(
        model = localUri,
        contentDescription = contentDescription,
        contentScale = contentScale,
        placeholder = androidx.compose.ui.res.painterResource(cc.chenhe.weargallery.R.drawable.bg_pic_default),
        error = androidx.compose.ui.res.painterResource(cc.chenhe.weargallery.R.drawable.bg_pic_default),
        modifier = modifier,
    )
}
