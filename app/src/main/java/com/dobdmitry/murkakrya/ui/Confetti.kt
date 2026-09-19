package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private class Fleck(
    val startX: Float,
    val delay: Float,
    val speed: Float,
    val drift: Float,
    val size: Float,
    val spin: Float,
    val color: Color,
    val isStar: Boolean,
)

/**
 * Конфетти и звёздочки на победу. Летит сверху вниз, слегка качаясь,
 * и исчезает само — трогать ничего не нужно.
 */
@Composable
fun ConfettiOverlay(
    playing: Boolean,
    seed: Int,
    modifier: Modifier = Modifier,
    count: Int = 70,
) {
    val progress = remember { Animatable(0f) }
    val flecks = remember(seed) {
        val random = Random(seed * 7919 + 13)
        List(count) {
            Fleck(
                startX = random.nextFloat(),
                delay = random.nextFloat() * 0.35f,
                speed = 0.75f + random.nextFloat() * 0.5f,
                drift = (random.nextFloat() - 0.5f) * 0.35f,
                size = 0.012f + random.nextFloat() * 0.020f,
                spin = (random.nextFloat() - 0.5f) * 900f,
                color = Palette.Confetti[random.nextInt(Palette.Confetti.size)],
                isStar = random.nextFloat() < 0.35f,
            )
        }
    }

    LaunchedEffect(playing, seed) {
        if (playing) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = 2600, easing = LinearEasing))
        } else {
            progress.snapTo(0f)
        }
    }

    if (progress.value <= 0f) return

    Canvas(modifier = modifier) {
        val t = progress.value
        for (fleck in flecks) {
            val local = ((t - fleck.delay) * fleck.speed).coerceAtLeast(0f)
            if (local <= 0f || local > 1.25f) continue
            val y = (-0.15f + local * 1.35f) * size.height
            val sway = sin(local * 9f + fleck.startX * 6f) * fleck.drift * size.width * 0.35f
            val x = fleck.startX * size.width + sway
            val alpha = (1f - (local - 0.75f).coerceAtLeast(0f) / 0.5f).coerceIn(0f, 1f)
            val side = fleck.size * size.width
            rotate(degrees = fleck.spin * local, pivot = Offset(x, y)) {
                if (fleck.isStar) {
                    drawStar(Offset(x, y), side, fleck.color.copy(alpha = alpha))
                } else {
                    drawRect(
                        color = fleck.color.copy(alpha = alpha),
                        topLeft = Offset(x - side / 2f, y - side / 2f),
                        size = Size(side, side * 1.6f),
                    )
                }
            }
        }
    }
}
