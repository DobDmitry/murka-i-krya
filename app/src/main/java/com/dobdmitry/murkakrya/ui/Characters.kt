package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Герои — готовые картинки лиц из art/, обработанные tools/make_faces.py.
 * Здесь только то, как они появляются на экране и как себя ведут.
 */

/** Лицо-картинка в круге нужного размера. */
fun DrawScope.drawFaceImage(image: ImageBitmap, center: Offset, radius: Float, alpha: Float = 1f) {
    val side = radius * 2.30f
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(
            (center.x - side / 2f).roundToInt(),
            (center.y - side / 2f).roundToInt(),
        ),
        dstSize = IntSize(side.roundToInt(), side.roundToInt()),
        alpha = alpha,
        filterQuality = FilterQuality.High,
    )
}

/**
 * Запасное лицо на случай, если картинка почему-то не загрузилась:
 * кружок цвета героя с улыбкой. Лучше улыбка, чем пустое место.
 */
fun DrawScope.drawSimpleFace(character: Cast, center: Offset, radius: Float, alpha: Float = 1f) {
    val body = character.color.copy(alpha = alpha)
    val dark = character.darkColor.copy(alpha = alpha)
    drawCircle(body, radius, center)
    drawCircle(dark, radius, center, style = Stroke(width = radius * 0.09f))
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Palette.Ink.copy(alpha = alpha),
            radius * 0.13f,
            Offset(center.x + side * radius * 0.34f, center.y - radius * 0.12f),
        )
    }
    drawArc(
        color = Palette.Ink.copy(alpha = alpha),
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.33f, center.y + radius * 0.16f),
        size = Size(radius * 0.66f, radius * 0.44f),
        style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round),
    )
}

fun DrawScope.drawCharacter(
    character: Cast,
    center: Offset,
    radius: Float,
    alpha: Float = 1f,
    image: ImageBitmap? = null,
) {
    if (image != null) {
        drawFaceImage(image, center, radius, alpha)
    } else {
        drawSimpleFace(character, center, radius, alpha)
    }
}

/** Настроение персонажа: от него зависит, как он себя ведёт. */
enum class Mood {
    /** Ждёт своей очереди: просто дышит. */
    IDLE,

    /** Его ход: подпрыгивает на месте, чтобы ребёнок видел, кого ждут. */
    ACTIVE,

    /** Выиграл: прыгает высоко и весело. */
    WINNER,

    /** Проиграл: вздыхает и качает головой — но не грустно. */
    LOSER,
}

/** Персонаж, который живёт сам по себе: дышит и реагирует на игру. */
@Composable
fun LivingCharacter(
    character: Cast,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    extraScale: Float = 1f,
    rotationDegrees: Float = 0f,
    mood: Mood = Mood.IDLE,
    seed: Int = 0,
) {
    val transition = rememberInfiniteTransition(label = "жизнь")
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700 + seed * 130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "дыхание",
    )
    val beat by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (mood) {
                    Mood.WINNER -> 340
                    Mood.ACTIVE -> 620
                    Mood.LOSER -> 1500
                    Mood.IDLE -> 2000
                },
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "настроение",
    )

    val image = LocalCastImages.current[character]

    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2f * 0.66f
        val base = Offset(size.width / 2f, size.height / 2f + radius * 0.10f)
        val alpha = if (dimmed) 0.32f else 1f

        val lift = when (mood) {
            Mood.WINNER -> -beat * radius * 0.30f
            Mood.ACTIVE -> -beat * radius * 0.12f
            Mood.LOSER -> beat * radius * 0.06f
            Mood.IDLE -> 0f
        }
        val sway = when (mood) {
            Mood.WINNER -> (beat - 0.5f) * 10f
            Mood.LOSER -> (beat - 0.5f) * 12f
            else -> 0f
        }
        val center = Offset(base.x, base.y + lift)

        rotate(rotationDegrees + sway, pivot = center) {
            scale(
                scaleX = (1f + breathe * 0.04f) * extraScale,
                scaleY = (1f + breathe * 0.05f) * extraScale,
                pivot = center,
            ) {
                drawCharacter(character, center, radius, alpha, image)
            }
        }
    }
}

/** Плоская картинка персонажа без анимации — для счёта и кнопок. */
@Composable
fun CharacterIcon(character: Cast, modifier: Modifier = Modifier, dimmed: Boolean = false) {
    val image = LocalCastImages.current[character]
    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2f * 0.64f
        val center = Offset(size.width / 2f, size.height / 2f + radius * 0.12f)
        drawCharacter(character, center, radius, alpha = if (dimmed) 0.30f else 1f, image = image)
    }
}

/** «Пыль» при приземлении фигуры. */
fun DrawScope.drawDust(center: Offset, radius: Float, progress: Float, color: Color) {
    if (progress <= 0f || progress >= 1f) return
    val spread = radius * (0.6f + progress * 1.4f)
    val alpha = (1f - progress) * 0.5f
    for (i in 0 until 7) {
        val angle = (i / 7f) * 2f * PI.toFloat()
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius * 0.16f * (1f - progress * 0.5f),
            center = Offset(
                center.x + cos(angle) * spread,
                center.y + radius * 0.75f + sin(angle) * spread * 0.35f,
            ),
        )
    }
}
