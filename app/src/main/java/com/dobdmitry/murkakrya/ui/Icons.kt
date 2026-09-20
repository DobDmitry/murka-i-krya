package com.dobdmitry.murkakrya.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Все иконки нарисованы фигурами: так они совпадают по стилю с персонажами. */

fun DrawScope.drawStar(center: Offset, radius: Float, color: Color, filled: Boolean = true) {
    val path = Path()
    for (i in 0 until 10) {
        val angle = (-PI / 2 + i * PI / 5).toFloat()
        val r = if (i % 2 == 0) radius else radius * 0.45f
        val x = center.x + cos(angle) * r
        val y = center.y + sin(angle) * r
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    if (filled) {
        drawPath(path, color)
        drawPath(path, color.copy(alpha = 0.55f), style = Stroke(width = radius * 0.14f))
    } else {
        drawPath(path, color.copy(alpha = 0.30f), style = Stroke(width = radius * 0.16f))
    }
}

fun DrawScope.drawRobot(center: Offset, radius: Float, alpha: Float = 1f) {
    val metal = Palette.Sky.copy(alpha = alpha)
    val dark = Palette.Ink.copy(alpha = alpha)
    val stroke = radius * 0.11f
    drawLine(
        dark,
        Offset(center.x, center.y - radius * 0.95f),
        Offset(center.x, center.y - radius * 1.35f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
    drawCircle(Palette.Pink.copy(alpha = alpha), radius * 0.16f, Offset(center.x, center.y - radius * 1.42f))
    drawRoundRect(
        color = metal,
        topLeft = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f),
        size = Size(radius * 1.8f, radius * 1.7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.42f),
    )
    drawRoundRect(
        color = dark,
        topLeft = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f),
        size = Size(radius * 1.8f, radius * 1.7f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.42f),
        style = Stroke(width = stroke),
    )
    for (side in listOf(-1f, 1f)) {
        drawCircle(dark, radius * 0.18f, Offset(center.x + side * radius * 0.38f, center.y - radius * 0.20f))
    }
    drawLine(
        dark,
        Offset(center.x - radius * 0.40f, center.y + radius * 0.38f),
        Offset(center.x + radius * 0.40f, center.y + radius * 0.38f),
        strokeWidth = stroke,
        cap = StrokeCap.Round,
    )
}

fun DrawScope.drawRepeat(center: Offset, radius: Float, color: Color) {
    val stroke = radius * 0.22f
    drawArc(
        color = color,
        startAngle = 40f,
        sweepAngle = 280f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.8f, center.y - radius * 0.8f),
        size = Size(radius * 1.6f, radius * 1.6f),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
    val tip = Offset(
        center.x + cos((40f * PI / 180f).toFloat()) * radius * 0.8f,
        center.y + sin((40f * PI / 180f).toFloat()) * radius * 0.8f,
    )
    val arrow = Path().apply {
        moveTo(tip.x - radius * 0.34f, tip.y - radius * 0.20f)
        lineTo(tip.x + radius * 0.30f, tip.y - radius * 0.26f)
        lineTo(tip.x - radius * 0.02f, tip.y + radius * 0.40f)
        close()
    }
    drawPath(arrow, color)
}

fun DrawScope.drawHome(center: Offset, radius: Float, color: Color) {
    val roof = Path().apply {
        moveTo(center.x - radius, center.y - radius * 0.05f)
        lineTo(center.x, center.y - radius * 0.95f)
        lineTo(center.x + radius, center.y - radius * 0.05f)
        close()
    }
    drawPath(roof, color)
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - radius * 0.70f, center.y - radius * 0.10f),
        size = Size(radius * 1.40f, radius * 1.00f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.16f),
    )
    drawRoundRect(
        color = Palette.Cream,
        topLeft = Offset(center.x - radius * 0.24f, center.y + radius * 0.26f),
        size = Size(radius * 0.48f, radius * 0.64f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.10f),
    )
}

fun DrawScope.drawSpeaker(center: Offset, radius: Float, on: Boolean, color: Color) {
    val stroke = radius * 0.18f
    val body = Path().apply {
        moveTo(center.x - radius * 0.75f, center.y - radius * 0.28f)
        lineTo(center.x - radius * 0.35f, center.y - radius * 0.28f)
        lineTo(center.x + radius * 0.10f, center.y - radius * 0.80f)
        lineTo(center.x + radius * 0.10f, center.y + radius * 0.80f)
        lineTo(center.x - radius * 0.35f, center.y + radius * 0.28f)
        lineTo(center.x - radius * 0.75f, center.y + radius * 0.28f)
        close()
    }
    drawPath(body, color)
    if (on) {
        for (i in 1..2) {
            drawArc(
                color = color,
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 0.1f - radius * 0.30f * i, center.y - radius * 0.35f * i),
                size = Size(radius * 0.60f * i, radius * 0.70f * i),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    } else {
        drawLine(
            color = Palette.Pink,
            start = Offset(center.x + radius * 0.30f, center.y - radius * 0.45f),
            end = Offset(center.x + radius * 0.95f, center.y + radius * 0.45f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Palette.Pink,
            start = Offset(center.x + radius * 0.95f, center.y - radius * 0.45f),
            end = Offset(center.x + radius * 0.30f, center.y + radius * 0.45f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/** Обёртка: иконка занимает весь отведённый размер. */
@Composable
fun IconCanvas(modifier: Modifier = Modifier, draw: DrawScope.(Offset, Float) -> Unit) {
    Canvas(modifier = modifier) {
        val radius = min(size.width, size.height) / 2f * 0.78f
        draw(this, Offset(size.width / 2f, size.height / 2f), radius)
    }
}

/** Значок раздела «играем вдвоём»: два лица рядом. */
@Composable
fun PeopleBadge(modifier: Modifier = Modifier) {
    val images = LocalCastImages.current
    IconCanvas(modifier) { center, radius ->
        drawCharacter(
            character = Cast.KIRA,
            center = Offset(center.x - radius * 0.46f, center.y + radius * 0.10f),
            radius = radius * 0.44f,
            image = images[Cast.KIRA],
        )
        drawCharacter(
            character = Cast.PAPA,
            center = Offset(center.x + radius * 0.50f, center.y + radius * 0.14f),
            radius = radius * 0.44f,
            image = images[Cast.PAPA],
        )
    }
}

/** Значок раздела «играем с компьютером»: лицо и робот. */
@Composable
fun RobotBadge(modifier: Modifier = Modifier) {
    val images = LocalCastImages.current
    IconCanvas(modifier) { center, radius ->
        drawCharacter(
            character = Cast.KIRA,
            center = Offset(center.x - radius * 0.48f, center.y + radius * 0.12f),
            radius = radius * 0.44f,
            image = images[Cast.KIRA],
        )
        drawRobot(Offset(center.x + radius * 0.52f, center.y + radius * 0.16f), radius * 0.44f)
    }
}

@Composable
fun StarIcon(modifier: Modifier = Modifier, color: Color = Palette.Krya, filled: Boolean = true) {
    IconCanvas(modifier) { center, radius -> drawStar(center, radius, color, filled) }
}

/**
 * Звёздочки силы соперника: одна — играет слабо, три — играет идеально.
 * Цифр и слов тут нет намеренно: звёздочки понятны без чтения.
 */
@Composable
fun StrengthStars(count: Int, modifier: Modifier = Modifier, total: Int = 3) {
    Canvas(modifier = modifier) {
        val step = size.width / total
        val radius = (step * 0.34f).coerceAtMost(size.height * 0.46f)
        for (i in 0 until total) {
            drawStar(
                center = Offset(step * (i + 0.5f), size.height / 2f),
                radius = radius,
                color = if (i < count) Palette.Fox else Palette.Pencil,
                filled = i < count,
            )
        }
    }
}

/** Сердечки для ничьей: показывают, что дружба важнее победы. */
fun DrawScope.drawHeart(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + radius * 0.75f)
        cubicTo(
            center.x - radius * 1.5f, center.y - radius * 0.2f,
            center.x - radius * 0.5f, center.y - radius * 1.2f,
            center.x, center.y - radius * 0.35f,
        )
        cubicTo(
            center.x + radius * 0.5f, center.y - radius * 1.2f,
            center.x + radius * 1.5f, center.y - radius * 0.2f,
            center.x, center.y + radius * 0.75f,
        )
        close()
    }
    drawPath(path, color)
}

fun DrawScope.drawHearts(progress: Float) {
    val base = size.minDimension * 0.10f
    val spots = listOf(0.30f to 0.30f, 0.50f to 0.18f, 0.70f to 0.34f)
    spots.forEachIndexed { index, spot ->
        val scale = 0.6f + 0.4f * ((index + 1) / 3f)
        drawHeart(
            center = Offset(size.width * spot.first, size.height * spot.second),
            radius = base * scale * progress,
            color = Palette.Pink.copy(alpha = 0.45f * progress),
        )
    }
}

/** Кубок за три победы подряд. */
fun DrawScope.drawTrophy(center: Offset, radius: Float) {
    val gold = Palette.Krya
    val dark = Palette.KryaDark

    // Ручки
    for (side in listOf(-1f, 1f)) {
        drawArc(
            color = dark,
            startAngle = if (side < 0) 90f else 270f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x + side * radius * 0.86f - radius * 0.26f, center.y - radius * 0.78f),
            size = Size(radius * 0.52f, radius * 0.84f),
            style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round),
        )
    }
    // Чаша
    val bowl = Path().apply {
        moveTo(center.x - radius * 0.62f, center.y - radius * 0.86f)
        lineTo(center.x + radius * 0.62f, center.y - radius * 0.86f)
        lineTo(center.x + radius * 0.44f, center.y + radius * 0.10f)
        cubicTo(
            center.x + radius * 0.30f, center.y + radius * 0.34f,
            center.x - radius * 0.30f, center.y + radius * 0.34f,
            center.x - radius * 0.44f, center.y + radius * 0.10f,
        )
        close()
    }
    drawPath(bowl, gold)
    drawPath(bowl, dark, style = Stroke(width = radius * 0.10f))
    // Ножка и подставка
    drawRect(
        color = gold,
        topLeft = Offset(center.x - radius * 0.14f, center.y + radius * 0.26f),
        size = Size(radius * 0.28f, radius * 0.36f),
    )
    drawRoundRect(
        color = dark,
        topLeft = Offset(center.x - radius * 0.52f, center.y + radius * 0.58f),
        size = Size(radius * 1.04f, radius * 0.26f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.10f),
    )
    // Звезда на чаше
    drawStar(Offset(center.x, center.y - radius * 0.34f), radius * 0.26f, Palette.Cream)
}

@Composable
fun TrophyIcon(modifier: Modifier = Modifier) {
    IconCanvas(modifier) { center, radius -> drawTrophy(center, radius * 0.9f) }
}
