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

package cc.chenhe.weargallery.ui.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.common.bean.Image
import cc.chenhe.weargallery.ui.compose.components.WearActionRow
import cc.chenhe.weargallery.ui.compose.components.WearPageHeader
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.foundation.layout.size
import cc.chenhe.weargallery.ui.compose.components.WearPageList
import cc.chenhe.weargallery.ui.pick.PickImageViewModel
import coil.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel

/** Local picture picker; [onPick] receives the tapped picture. */
@Composable
fun PickImageScreen(onPick: (Image) -> Unit) {
    val model: PickImageViewModel = koinViewModel()
    val data by model.data.observeAsState()
    val folders by model.folders.observeAsState()
    val title by model.currentBucketTitle.observeAsState()
    var showFolderChooser by remember { mutableStateOf(false) }

    val images = data?.data

    if (showFolderChooser) {
        WearPageList { spec ->
            item { WearPageHeader(spec, stringResource(R.string.pick_image_folder)) }
            item {
                WearActionRow(
                    spec,
                    label = stringResource(R.string.pick_image_all),
                    onClick = {
                        model.setBucketId(null)
                        showFolderChooser = false
                    },
                )
            }
            folders.orEmpty().forEach { folder ->
                item(key = "folder-${folder.id}") {
                    WearActionRow(
                        spec,
                        label = folder.name,
                        onClick = {
                            model.setBucketId(folder.id)
                            showFolderChooser = false
                        },
                    )
                }
            }
        }
        return
    }

    WearPageList(
        edgeButton = {
            EdgeIcon(
                icon = Icons.Rounded.Folder,
                description = stringResource(R.string.pick_image_folder),
                onClick = { showFolderChooser = true },
            )
        },
    ) { spec ->
        item { WearPageHeader(spec, title ?: stringResource(R.string.pick_image_all)) }
        if (images.isNullOrEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.local_empty),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            images.chunked(2).forEachIndexed { rowIndex, row ->
                item(key = "pick-row-$rowIndex") {
                    Row(
                        modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        row.forEach { image ->
                            PickCell(
                                image = image,
                                modifier = Modifier.weight(1f),
                                onClick = { onPick(image) },
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }


}

@Composable
private fun TransformingLazyColumnItemScope.PickCell(
    image: Image,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    AsyncImage(
        model = image.uri,
        contentDescription = image.name,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    )
}



@Composable
private fun EdgeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(24.dp))
    }
}
