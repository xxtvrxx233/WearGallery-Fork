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

package cc.chenhe.weargallery.ui.compose

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

/**
 * Theme preferences backed by SharedPreferences, modelled on the Compass app: the user can follow
 * the system accent color or pick a seed color, palette style and color spec.
 */
object WearThemePreferences {

    private const val PREFS_NAME = "weargallery_theme"
    private const val KEY_DYNAMIC = "use_dynamic_color"
    private const val KEY_SEED = "seed_color"
    private const val KEY_STYLE = "palette_style"
    private const val KEY_SPEC = "color_spec"

    const val DEFAULT_SEED_COLOR = 0xFF4CD964.toInt()

    val useDynamicColor = mutableStateOf(true)
    val seedColor = mutableIntStateOf(DEFAULT_SEED_COLOR)
    val paletteStyle = mutableStateOf(PaletteStyle.TonalSpot)
    val specVersion = mutableStateOf(ColorSpec.SpecVersion.SPEC_2021)

    private var prefs: android.content.SharedPreferences? = null

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        useDynamicColor.value = p.getBoolean(KEY_DYNAMIC, true)
        seedColor.intValue = p.getInt(KEY_SEED, DEFAULT_SEED_COLOR)
        paletteStyle.value = runCatching {
            PaletteStyle.valueOf(p.getString(KEY_STYLE, PaletteStyle.TonalSpot.name)!!)
        }.getOrDefault(PaletteStyle.TonalSpot)
        specVersion.value = runCatching {
            ColorSpec.SpecVersion.valueOf(
                p.getString(KEY_SPEC, ColorSpec.SpecVersion.SPEC_2021.name)!!
            )
        }.getOrDefault(ColorSpec.SpecVersion.SPEC_2021)
    }

    fun setDynamicColor(dynamic: Boolean) {
        useDynamicColor.value = dynamic
        prefs?.edit()?.putBoolean(KEY_DYNAMIC, dynamic)?.apply()
    }

    fun setSeedColor(color: Int) {
        seedColor.intValue = color
        prefs?.edit()?.putInt(KEY_SEED, color)?.apply()
    }

    /** A seed color only takes effect when dynamic color is off. */
    fun selectColor(color: Int) {
        setSeedColor(color)
        setDynamicColor(false)
    }

    fun setPaletteStyle(style: PaletteStyle) {
        paletteStyle.value = style
        prefs?.edit()?.putString(KEY_STYLE, style.name)?.apply()
    }

    fun setSpecVersion(version: ColorSpec.SpecVersion) {
        specVersion.value = version
        prefs?.edit()?.putString(KEY_SPEC, version.name)?.apply()
    }
}
