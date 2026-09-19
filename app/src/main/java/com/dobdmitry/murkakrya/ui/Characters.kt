package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import murka.core.Player
import kotlin.math.min

/**
 * Персонажи нарисованы простыми фигурами прямо здесь — никаких чужих картинок.
 * Хотите другого зверя: меняете тело этих двух функций, и он появится везде сразу.
 */

/** Котик МУРКА. [blink] = 1 — глаза открыты, 0 — закрыты. */
fun DrawScope.drawMurka(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val body = Palette.Murka.copy(alpha = alpha)
    val dark = Palette.MurkaDark.copy(alpha = alpha)
    val pink = Palette.Pink.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)
    val outline = radius * 0.10f

    // Ушки
    for (side in listOf(-1f, 1f)) {
        val ear = Path().apply {
            moveTo(center.x + side * radius * 0.30f, center.y - radius * 0.72f)
            lineTo(center.x + side * radius * 0.74f, center.y - radius * 1.28f)
            lineTo(center.x + side * radius * 0.92f, center.y - radius * 0.44f)
            close()
        }
        drawPath(ear, body)
        drawPath(ear, dark, style = Stroke(width = outline))
        val inner = Path().apply {
            moveTo(center.x + side * radius * 0.45f, center.y - radius * 0.76f)
            lineTo(center.x + side * radius * 0.70f, center.y - radius * 1.06f)
            lineTo(center.x + side * radius * 0.78f, center.y - radius * 0.66f)
            close()
        }
        drawPath(inner, pink)
    }

    // Голова
    drawCircle(body, radius, center)
    drawCircle(dark, radius, center, style = Stroke(width = outline))

    // Полоски на лбу
    for (i in -1..1) {
        val x = center.x + i * radius * 0.30f
        drawLine(
            color = dark,
            start = Offset(x, center.y - radius * 0.74f),
            end = Offset(x + radius * 0.06f, center.y - radius * 0.44f),
            strokeWidth = outline * 0.8f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }

    // Глаза
    for (side in listOf(-1f, 1f)) {
        val eye = Offset(center.x + side * radius * 0.36f, center.y - radius * 0.08f)
        if (blink > 0.12f) {
            drawOval(
                color = ink,
                topLeft = Offset(eye.x - radius * 0.13f, eye.y - radius * 0.19f * blink),
                size = Size(radius * 0.26f, radius * 0.38f * blink),
            )
            drawCircle(Palette.Cream.copy(alpha = alpha), radius * 0.07f * blink,
                Offset(eye.x + radius * 0.05f, eye.y - radius * 0.10f * blink))
        } else {
            drawLine(
                color = ink,
                start = Offset(eye.x - radius * 0.15f, eye.y),
                end = Offset(eye.x + radius * 0.15f, eye.y),
                strokeWidth = outline,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }

    // Щёчки
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            pink.copy(alpha = 0.45f * alpha),
            radius * 0.18f,
            Offset(center.x + side * radius * 0.62f, center.y + radius * 0.30f),
        )
    }

    // Нос
    val nose = Path().apply {
        moveTo(center.x - radius * 0.13f, center.y + radius * 0.24f)
        lineTo(center.x + radius * 0.13f, center.y + radius * 0.24f)
        lineTo(center.x, center.y + radius * 0.40f)
        close()
    }
    drawPath(nose, pink)

    // Улыбка
    drawArc(
        color = ink,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.34f, center.y + radius * 0.22f),
        size = Size(radius * 0.68f, radius * 0.46f),
        style = Stroke(width = outline * 0.8f, cap = androidx.compose.ui.graphics.StrokeCap.Round),
    )

    // Усы
    for (side in listOf(-1f, 1f)) {
        for (i in 0..1) {
            drawLine(
                color = dark,
                start = Offset(center.x + side * radius * 0.30f, center.y + radius * 0.34f + i * radius * 0.14f),
                end = Offset(center.x + side * radius * 0.95f, center.y + radius * (0.24f + i * 0.26f)),
                strokeWidth = outline * 0.6f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }
}

/** Утёнок КРЯ. */
fun DrawScope.drawKrya(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val body = Palette.Krya.copy(alpha = alpha)
    val dark = Palette.KryaDark.copy(alpha = alpha)
    val beak = Palette.Murka.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)
    val outline = radius * 0.10f

    // Хохолок
    for (i in -1..1) {
        drawArc(
            color = dark,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(center.x + i * radius * 0.26f - radius * 0.16f, center.y - radius * 1.24f),
            size = Size(radius * 0.32f, radius * 0.46f),
            style = Stroke(width = outline, cap = androidx.compose.ui.graphics.StrokeCap.Round),
        )
    }

    // Голова
    drawCircle(body, radius, center)
    drawCircle(dark, radius, center, style = Stroke(width = outline))

    // Глаза
    for (side in listOf(-1f, 1f)) {
        val eye = Offset(center.x + side * radius * 0.32f, center.y - radius * 0.18f)
        if (blink > 0.12f) {
            drawCircle(ink, radius * 0.15f * blink.coerceAtLeast(0.35f), eye)
            drawCircle(
                Palette.Cream.copy(alpha = alpha),
                radius * 0.05f * blink,
                Offset(eye.x + radius * 0.05f, eye.y - radius * 0.05f),
            )
        } else {
            drawLine(
                color = ink,
                start = Offset(eye.x - radius * 0.15f, eye.y),
                end = Offset(eye.x + radius * 0.15f, eye.y),
                strokeWidth = outline,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
    }

    // Клюв
    val beakRect = Rect(
        Offset(center.x - radius * 0.42f, center.y + radius * 0.10f),
        Size(radius * 0.84f, radius * 0.46f),
    )
    drawOval(beak, beakRect.topLeft, beakRect.size)
    drawOval(Palette.MurkaDark.copy(alpha = alpha), beakRect.topLeft, beakRect.size, style = Stroke(width = outline * 0.7f))
    drawLine(
        color = Palette.MurkaDark.copy(alpha = alpha),
        start = Offset(beakRect.left + radius * 0.10f, beakRect.center.y + radius * 0.02f),
        end = Offset(beakRect.right - radius * 0.10f, beakRect.center.y + radius * 0.02f),
        strokeWidth = outline * 0.6f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
    )

    // Щёчки
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Palette.Pink.copy(alpha = 0.40f * alpha),
            radius * 0.16f,
            Offset(center.x + side * radius * 0.66f, center.y + radius * 0.16f),
        )
    }
}

/** Рисует нужного персонажа. */
fun DrawScope.drawPlayer(
    player: Player,
    center: Offset,
    radius: Float,
    blink: Float = 1f,
    alpha: Float = 1f,
) {
    when (player) {
        Player.MURKA -> drawMurka(center, radius, blink, alpha)
        Player.KRYA -> drawKrya(center, radius, blink, alpha)
    }
}

/**
 * Персонаж, который живёт сам по себе: дышит и моргает.
 * Именно это делает пустое ожидание хода нескучным.
 */
@Composable
fun LivingCharacter(
    player: Player,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    extraScale: Float = 1f,
    rotationDegrees: Float = 0f,
    seed: Int = 0,
) {
    val transition = rememberInfiniteTransition(label = "жизнь")
    val breathe by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1700 + seed * 130, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "дыхание",
    )
    val blink by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600 + seed * 370
                1f at 0
                1f at (3100 + seed * 300)
                0f at (3220 + seed * 300)
                1f at (3340 + seed * 300)
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "моргание",
    )

    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2f * 0.72f
        val center = Offset(size.width / 2f, size.height / 2f + radius * 0.10f)
        val alpha = if (dimmed) 0.32f else 1f
        rotate(rotationDegrees, pivot = center) {
            scale(scaleX = (1f + breathe * 0.04f) * extraScale, scaleY = (1f + breathe * 0.05f) * extraScale, pivot = center) {
                drawPlayer(player, center, radius, blink, alpha)
            }
        }
    }
}

/** Плоская иконка персонажа без анимации — для счёта и кнопок. */
@Composable
fun CharacterIcon(player: Player, modifier: Modifier = Modifier, dimmed: Boolean = false) {
    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2f * 0.70f
        val center = Offset(size.width / 2f, size.height / 2f + radius * 0.12f)
        drawPlayer(player, center, radius, alpha = if (dimmed) 0.30f else 1f)
    }
}

/** Общая заливка «пыли» — используется при приземлении фигуры. */
fun DrawScope.drawDust(center: Offset, radius: Float, progress: Float, color: Color) {
    if (progress <= 0f || progress >= 1f) return
    val spread = radius * (0.6f + progress * 1.4f)
    val alpha = (1f - progress) * 0.5f
    for (i in 0 until 7) {
        val angle = (i / 7f) * 2f * Math.PI.toFloat()
        val dx = kotlin.math.cos(angle) * spread
        val dy = kotlin.math.sin(angle) * spread * 0.35f
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius * 0.16f * (1f - progress * 0.5f),
            center = Offset(center.x + dx, center.y + radius * 0.75f + dy),
        )
    }
}
