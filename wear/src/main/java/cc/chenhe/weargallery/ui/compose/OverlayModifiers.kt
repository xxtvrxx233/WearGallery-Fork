/**
 * Copyright (C) 2026 xxtvrxx233
 * Portions adapted from WearCompass (GPL-3.0), Copyright (C) 2026 xxtvrxx233.
 *
 * This file is distributed under the Wear Gallery Public License v1 (AGPL-3.0 with additional
 * terms). See the LICENSE file in the repository root.
 */

package cc.chenhe.weargallery.ui.compose

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp

/**
 * Dims and shrinks a lower layer while an overlay is on top of it.
 * [progress] is read in the draw phase, so callers can drive it from an [Animatable] without
 * triggering recomposition of the content.
 */
internal fun Modifier.coveredLayer(progress: () -> Float): Modifier =
    this
        .graphicsLayer {
            val covered = (1f - progress()).coerceIn(0f, 1f)
            val scale = 1f - 0.12f * covered
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            val covered = (1f - progress()).coerceIn(0f, 1f)
            if (covered > 0f) {
                drawRect(Color.Black.copy(alpha = 0.5f * covered))
            }
        }

/**
 * The Wear dismiss animation copied from Compass: the layer scales down to 0.7, only slides out
 * in the last 30% of the gesture, draws a circular scrim and clips itself to a circle on round
 * screens. [progress] 0 = fully presented, 1 = dismissed; it is read in the draw phase.
 */
internal fun Modifier.wearDismissLayer(
    progress: () -> Float,
    widthPx: Float,
    isRound: Boolean,
): Modifier =
    this
        .graphicsLayer {
            val value = progress()
            val scale = lerp(1f, 0.7f, value)
            val squeezeOffset = maxOf(0f, (1f - scale) * widthPx / 2f)
            val slideFactor = ((value - 0.7f) / 0.3f).coerceIn(0f, 1f)
            scaleX = scale
            scaleY = scale
            translationX = lerp(squeezeOffset, widthPx, slideFactor)
        }
        .drawWithContent {
            drawContent()
            val value = progress()
            val scrim = (value / 2f).coerceAtMost(0.3f)
            if (scrim > 0f) {
                val color = Color.Black.copy(alpha = scrim)
                if (isRound) {
                    drawCircle(color = color, radius = size.minDimension / 2f)
                } else {
                    drawRect(color = color)
                }
            }
        }
        .graphicsLayer {
            if (isRound && progress() > 0.001f) {
                clip = true
                shape = CircleShape
            }
        }
