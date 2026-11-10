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

package cc.chenhe.weargallery.ui.compose

import android.os.Build
import androidx.compose.material3.ColorScheme as MaterialColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.MotionScheme
import androidx.wear.compose.material3.Typography
import com.materialkolor.dynamicColorScheme

/**
 * Wear Material 3 theme. The palette is generated with materialkolor from either the system accent
 * color or the user's seed color, using the palette style and color spec chosen in the theme page —
 * the same logic as the Compass app.
 */
@Composable
fun WearGalleryTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dynamic = WearThemePreferences.useDynamicColor.value
    val seed = WearThemePreferences.seedColor.intValue
    val style = WearThemePreferences.paletteStyle.value
    val spec = WearThemePreferences.specVersion.value

    val resolvedSeed = remember(dynamic, seed) {
        if (dynamic) systemSeedColor(context) ?: Color(seed) else Color(seed)
    }
    val colors = remember(resolvedSeed, style, spec) {
        runCatching {
            dynamicColorScheme(
                seedColor = resolvedSeed,
                isDark = true,
                style = style,
                specVersion = spec,
            ).toWearColorScheme()
        }.getOrElse { ColorScheme() }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

private fun systemSeedColor(context: android.content.Context): Color? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    return runCatching {
        Color(ContextCompat.getColor(context, android.R.color.system_accent1_500))
    }.getOrNull()
}

private fun MaterialColorScheme.toWearColorScheme(): ColorScheme = ColorScheme(
    primary = primaryFixed,
    primaryDim = primaryFixedDim,
    primaryContainer = primaryContainer,
    onPrimary = onPrimaryFixed,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondaryFixed,
    secondaryDim = secondaryFixedDim,
    secondaryContainer = secondaryContainer,
    onSecondary = onSecondaryFixed,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiaryFixed,
    tertiaryDim = tertiaryFixedDim,
    tertiaryContainer = tertiaryContainer,
    onTertiary = onTertiaryFixed,
    onTertiaryContainer = onTertiaryContainer,
    surfaceContainerLow = surfaceContainerLow,
    surfaceContainer = surfaceContainer,
    surfaceContainerHigh = surfaceContainerHigh,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    outlineVariant = outlineVariant,
    background = Color.Black,
    onBackground = onBackground,
    error = error,
    errorDim = errorContainer,
    errorContainer = errorContainer,
    onError = onError,
    onErrorContainer = onErrorContainer,
)
