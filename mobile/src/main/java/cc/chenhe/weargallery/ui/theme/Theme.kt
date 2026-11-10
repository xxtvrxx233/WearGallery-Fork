package cc.chenhe.weargallery.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat.getInsetsController
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

/** Brand seed used when Material You colors are unavailable. */
private val BrandSeed = Color(0xFFBF002D)

/**
 * Material 3 Expressive theme following the OTA2 design: a tonal-spot palette generated from the
 * system accent color (or the brand seed), expressive motion and the expressive component defaults.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WearGalleryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(darkTheme, dynamicColor) {
        val seed = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                Color(ContextCompat.getColor(context, android.R.color.system_accent1_500))
            }.getOrDefault(BrandSeed)
        } else {
            BrandSeed
        }
        runCatching {
            dynamicColorScheme(
                seedColor = seed,
                isDark = darkTheme,
                style = PaletteStyle.TonalSpot,
                specVersion = ColorSpec.SpecVersion.SPEC_2021,
            )
        }.getOrElse {
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // set status bar to transparent and adapt icon color to dark/light theme.
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
