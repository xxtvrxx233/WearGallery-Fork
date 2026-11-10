/**
 * Copyright (C) 2026 xxtvrxx233
 * Portions adapted from WearCompass (GPL-3.0), Copyright (C) 2026 xxtvrxx233.
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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.ListSubHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import cc.chenhe.weargallery.R
import cc.chenhe.weargallery.ui.compose.WearThemePreferences
import cc.chenhe.weargallery.ui.compose.components.WearPageHeader
import cc.chenhe.weargallery.ui.compose.components.WearPageList
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

private val SwatchColors = listOf(
    0xFFFF9FCAFF, 0xFFBEC2FF, 0xFFD6BBFF,
    0xFFBF8D9D, 0xFFE6ACCC, 0xFFFFB3A5,
    0xFFFFD166, 0xFFE9EF80, 0xFF9ACB78,
    0xFF75CBBA, 0xFF44CBE0, 0xFFB5C4CD,
).map { Color(it) }

/** Theme color page, mirroring the Compass app: main page plus two choice sub-pages. */
@Composable
fun ThemeSettingsScreen(
    onNavigate: (cc.chenhe.weargallery.ui.compose.WearRoute) -> Unit,
    onBack: () -> Unit,
) {
    ThemeColorMainPage(
        onOpenSubPage = { kind ->
            onNavigate(cc.chenhe.weargallery.ui.compose.WearRoute.ThemeChoice(kind))
        },
        onBack = onBack,
    )
}

/** Palette style / color spec choice page, opened as a sub-overlay. */
@Composable
fun ThemeChoiceScreen(kind: String, onBack: () -> Unit) {
    when (kind) {
        "palette" -> ChoicePage(
            title = stringResource(R.string.theme_palette_style),
            choices = PaletteStyle.entries.map { it.name to paletteStyleLabel(it) },
            selected = WearThemePreferences.paletteStyle.value.name,
            onChoose = { WearThemePreferences.setPaletteStyle(PaletteStyle.valueOf(it)) },
            onBack = onBack,
        )

        else -> ChoicePage(
            title = stringResource(R.string.theme_color_spec),
            choices = ColorSpec.SpecVersion.entries.map { it.name to specVersionLabel(it) },
            selected = WearThemePreferences.specVersion.value.name,
            onChoose = { WearThemePreferences.setSpecVersion(ColorSpec.SpecVersion.valueOf(it)) },
            onBack = onBack,
        )
    }
}

@Composable
private fun ThemeColorMainPage(
    onOpenSubPage: (String) -> Unit,
    onBack: () -> Unit,
) {
    val dynamic = WearThemePreferences.useDynamicColor.value
    val seed = Color(WearThemePreferences.seedColor.intValue)
    val style = WearThemePreferences.paletteStyle.value
    val spec = WearThemePreferences.specVersion.value

    WearPageList(
        edgeButton = {
            EdgeButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
    ) { transformationSpec ->
        item { WearPageHeader(transformationSpec, stringResource(R.string.theme_color_title)) }

        item {
            SwitchButton(
                checked = dynamic,
                onCheckedChange = WearThemePreferences::setDynamicColor,
                label = { Text(stringResource(R.string.theme_follow_system)) },
                secondaryLabel = {
                    Text(
                        if (dynamic) {
                            stringResource(R.string.theme_follow_system_on)
                        } else {
                            stringResource(R.string.theme_follow_system_off)
                        }
                    )
                },
                icon = {
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                },
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
            )
        }

        item {
            FilledTonalButton(
                onClick = { onOpenSubPage("palette") },
                label = { Text(stringResource(R.string.theme_palette_style)) },
                secondaryLabel = { Text(paletteStyleLabel(style)) },
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
            )
        }

        item {
            FilledTonalButton(
                onClick = { onOpenSubPage("spec") },
                label = { Text(stringResource(R.string.theme_color_spec)) },
                secondaryLabel = { Text(specVersionLabel(spec)) },
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
            )
        }

        item {
            ListSubHeader(
                modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                transformation = SurfaceTransformation(transformationSpec),
            ) {
                Text(stringResource(R.string.theme_swatches))
            }
        }

        SwatchColors.chunked(3).forEachIndexed { rowIndex, row ->
            item(key = "swatch-$rowIndex") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    row.forEach { color ->
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ColorSwatch(
                                color = color,
                                selected = !dynamic && seed == color,
                                onClick = { WearThemePreferences.selectColor(color.toArgb()) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoicePage(
    title: String,
    choices: List<Pair<String, String>>,
    selected: String,
    onChoose: (String) -> Unit,
    onBack: () -> Unit,
) {
    WearPageList(
        edgeButton = {
            EdgeButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
        },
    ) { transformationSpec ->
        item { WearPageHeader(transformationSpec, title) }
        choices.forEach { (value, label) ->
            item(key = "choice-$value") {
                RadioButton(
                    selected = value == selected,
                    onSelect = { onChoose(value) },
                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                    transformation = SurfaceTransformation(transformationSpec),
                    contentPadding = ButtonDefaults.ContentPadding,
                    label = { Text(label, maxLines = 3, overflow = TextOverflow.Ellipsis) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val checkTint = if (color.luminance() > 0.45f) Color.Black else Color.White
    IconToggleButton(
        checked = selected,
        onCheckedChange = { onClick() },
        modifier = Modifier.fillMaxWidth(0.9f).heightIn(min = 48.dp),
        colors = IconToggleButtonDefaults.colors(
            checkedContainerColor = color,
            checkedContentColor = checkTint,
            uncheckedContainerColor = color,
            uncheckedContentColor = Color.Transparent,
        ),
        shapes = IconToggleButtonDefaults.variantAnimatedShapes(),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) else null,
    ) {
        Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = if (selected) checkTint else Color.Transparent,
        )
    }
}

@Composable
private fun paletteStyleLabel(style: PaletteStyle): String = stringResource(
    when (style) {
        PaletteStyle.TonalSpot -> R.string.palette_tonal_spot
        PaletteStyle.Neutral -> R.string.palette_neutral
        PaletteStyle.Vibrant -> R.string.palette_vibrant
        PaletteStyle.Expressive -> R.string.palette_expressive
        PaletteStyle.Rainbow -> R.string.palette_rainbow
        PaletteStyle.FruitSalad -> R.string.palette_fruit_salad
        PaletteStyle.Monochrome -> R.string.palette_monochrome
        PaletteStyle.Fidelity -> R.string.palette_fidelity
        PaletteStyle.Content -> R.string.palette_content
    },
)

@Composable
private fun specVersionLabel(version: ColorSpec.SpecVersion): String = stringResource(
    when (version) {
        ColorSpec.SpecVersion.SPEC_2021 -> R.string.spec_2021
        ColorSpec.SpecVersion.SPEC_2025 -> R.string.spec_2025
    },
)
