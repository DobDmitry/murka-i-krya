package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
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
 * Все персонажи нарисованы простыми фигурами прямо здесь — ни одной чужой картинки.
 * Хотите нового героя: добавляете его в [Cast] и пишете тело рисующей функции.
 */

// --- Общие части лица -------------------------------------------------------

private fun DrawScope.eyes(
    center: Offset,
    radius: Float,
    blink: Float,
    ink: Color,
    spread: Float = 0.34f,
    height: Float = -0.10f,
    size: Float = 0.13f,
) {
    for (side in listOf(-1f, 1f)) {
        val eye = Offset(center.x + side * radius * spread, center.y + radius * height)
        if (blink > 0.12f) {
            drawCircle(ink, radius * size * blink.coerceAtLeast(0.4f), eye)
            drawCircle(
                Palette.Cream.copy(alpha = 0.9f),
                radius * size * 0.34f,
                Offset(eye.x + radius * 0.04f, eye.y - radius * 0.04f),
            )
        } else {
            drawLine(
                color = ink,
                start = Offset(eye.x - radius * 0.14f, eye.y),
                end = Offset(eye.x + radius * 0.14f, eye.y),
                strokeWidth = radius * 0.09f,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.smile(center: Offset, radius: Float, ink: Color, width: Float = 0.66f, top: Float = 0.18f) {
    drawArc(
        color = ink,
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - radius * width / 2f, center.y + radius * top),
        size = Size(radius * width, radius * 0.44f),
        style = Stroke(width = radius * 0.08f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.blush(center: Offset, radius: Float, alpha: Float, spread: Float = 0.62f, height: Float = 0.22f) {
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Palette.Pink.copy(alpha = 0.40f * alpha),
            radius * 0.17f,
            Offset(center.x + side * radius * spread, center.y + radius * height),
        )
    }
}

private fun DrawScope.head(center: Offset, radius: Float, fill: Color, outline: Color) {
    drawCircle(fill, radius, center)
    drawCircle(outline, radius, center, style = Stroke(width = radius * 0.09f))
}

private fun DrawScope.pointedEars(center: Offset, radius: Float, fill: Color, outline: Color, inner: Color) {
    for (side in listOf(-1f, 1f)) {
        val ear = Path().apply {
            moveTo(center.x + side * radius * 0.30f, center.y - radius * 0.72f)
            lineTo(center.x + side * radius * 0.76f, center.y - radius * 1.30f)
            lineTo(center.x + side * radius * 0.94f, center.y - radius * 0.42f)
            close()
        }
        drawPath(ear, fill)
        drawPath(ear, outline, style = Stroke(width = radius * 0.09f))
        val hole = Path().apply {
            moveTo(center.x + side * radius * 0.46f, center.y - radius * 0.74f)
            lineTo(center.x + side * radius * 0.71f, center.y - radius * 1.06f)
            lineTo(center.x + side * radius * 0.79f, center.y - radius * 0.64f)
            close()
        }
        drawPath(hole, inner)
    }
}

private fun DrawScope.muzzle(center: Offset, radius: Float, fill: Color, nose: Color) {
    drawOval(
        color = fill,
        topLeft = Offset(center.x - radius * 0.46f, center.y + radius * 0.06f),
        size = Size(radius * 0.92f, radius * 0.66f),
    )
    drawOval(
        color = nose,
        topLeft = Offset(center.x - radius * 0.14f, center.y + radius * 0.10f),
        size = Size(radius * 0.28f, radius * 0.20f),
    )
}

// --- Люди -------------------------------------------------------------------

/** КИРА: два хвостика и бантик. */
fun DrawScope.drawKira(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val skin = Palette.Skin.copy(alpha = alpha)
    val hair = Palette.KiraHair.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    // Хвостики
    for (side in listOf(-1f, 1f)) {
        drawCircle(hair, radius * 0.36f, Offset(center.x + side * radius * 0.96f, center.y - radius * 0.44f))
    }
    head(center, radius, skin, hair)
    // Чёлка
    drawArc(
        color = hair,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 1.16f),
    )
    drawArc(
        color = hair,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.95f, center.y - radius * 0.70f),
        size = Size(radius * 1.9f, radius * 1.0f),
        style = Stroke(width = radius * 0.22f, cap = StrokeCap.Round),
    )
    // Бантик
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Palette.Pink.copy(alpha = alpha),
            radius * 0.17f,
            Offset(center.x - radius * 0.86f + side * radius * 0.17f, center.y - radius * 0.86f),
        )
    }
    drawCircle(Palette.PinkDark.copy(alpha = alpha), radius * 0.08f, Offset(center.x - radius * 0.86f, center.y - radius * 0.86f))

    eyes(center, radius, blink, ink, size = 0.15f)
    blush(center, radius, alpha)
    smile(center, radius, ink)
}

/** МАМА: длинные волосы и серёжки. */
fun DrawScope.drawMama(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val skin = Palette.Skin.copy(alpha = alpha)
    val hair = Palette.MamaHair.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    // Длинные волосы по бокам
    for (side in listOf(-1f, 1f)) {
        drawOval(
            color = hair,
            topLeft = Offset(center.x + side * radius * 0.62f - radius * 0.34f, center.y - radius * 0.90f),
            size = Size(radius * 0.68f, radius * 1.90f),
        )
    }
    head(center, radius, skin, hair)
    drawArc(
        color = hair,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 1.05f),
    )
    // Серёжки
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Palette.Krya.copy(alpha = alpha),
            radius * 0.10f,
            Offset(center.x + side * radius * 0.94f, center.y + radius * 0.26f),
        )
    }
    eyes(center, radius, blink, ink)
    blush(center, radius, alpha, spread = 0.56f)
    smile(center, radius, ink)
}

/** ПАПА: короткие волосы и борода. */
fun DrawScope.drawPapa(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val skin = Palette.SkinWarm.copy(alpha = alpha)
    val hair = Palette.PapaHair.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    head(center, radius, skin, hair)
    // Волосы шапочкой
    drawArc(
        color = hair,
        startAngle = 182f,
        sweepAngle = 176f,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 0.94f),
    )
    // Борода
    drawArc(
        color = hair,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = true,
        topLeft = Offset(center.x - radius * 0.92f, center.y - radius * 0.35f),
        size = Size(radius * 1.84f, radius * 1.60f),
    )
    // Брови
    for (side in listOf(-1f, 1f)) {
        drawLine(
            color = hair,
            start = Offset(center.x + side * radius * 0.20f, center.y - radius * 0.34f),
            end = Offset(center.x + side * radius * 0.50f, center.y - radius * 0.30f),
            strokeWidth = radius * 0.10f,
            cap = StrokeCap.Round,
        )
    }
    eyes(center, radius, blink, ink, height = -0.08f)
    // Улыбка внутри бороды
    drawArc(
        color = Palette.PinkDark.copy(alpha = alpha),
        startAngle = 15f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.30f, center.y + radius * 0.24f),
        size = Size(radius * 0.60f, radius * 0.36f),
        style = Stroke(width = radius * 0.10f, cap = StrokeCap.Round),
    )
}

// --- Звери ------------------------------------------------------------------

/** КРЯ: утёнок с клювом. */
fun DrawScope.drawKrya(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val body = Palette.Krya.copy(alpha = alpha)
    val dark = Palette.KryaDark.copy(alpha = alpha)
    val beak = Palette.Beak.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    for (i in -1..1) {
        drawArc(
            color = dark,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(center.x + i * radius * 0.26f - radius * 0.16f, center.y - radius * 1.24f),
            size = Size(radius * 0.32f, radius * 0.46f),
            style = Stroke(width = radius * 0.10f, cap = StrokeCap.Round),
        )
    }
    head(center, radius, body, dark)
    eyes(center, radius, blink, ink, spread = 0.30f, height = -0.20f, size = 0.14f)
    drawOval(
        color = beak,
        topLeft = Offset(center.x - radius * 0.42f, center.y + radius * 0.10f),
        size = Size(radius * 0.84f, radius * 0.46f),
    )
    drawLine(
        color = Palette.BeakDark.copy(alpha = alpha),
        start = Offset(center.x - radius * 0.32f, center.y + radius * 0.34f),
        end = Offset(center.x + radius * 0.32f, center.y + radius * 0.34f),
        strokeWidth = radius * 0.06f,
        cap = StrokeCap.Round,
    )
    blush(center, radius, alpha, spread = 0.66f, height = 0.16f)
}

/** ЁЖИК: колючки по макушке. */
fun DrawScope.drawHedgehog(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val needles = Palette.HedgehogDark.copy(alpha = alpha)
    val face = Palette.Hedgehog.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    for (i in 0 until 11) {
        val angle = (PI + i * PI / 10).toFloat()
        val base = Offset(center.x + cos(angle) * radius * 0.92f, center.y + sin(angle) * radius * 0.92f)
        val tip = Offset(center.x + cos(angle) * radius * 1.34f, center.y + sin(angle) * radius * 1.34f)
        drawLine(needles, base, tip, strokeWidth = radius * 0.14f, cap = StrokeCap.Round)
    }
    head(center, radius, face, needles)
    drawArc(
        color = needles,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 1.10f),
    )
    eyes(center, radius, blink, ink, spread = 0.30f, height = 0.02f, size = 0.12f)
    drawOval(
        color = Palette.Ink.copy(alpha = alpha),
        topLeft = Offset(center.x - radius * 0.12f, center.y + radius * 0.40f),
        size = Size(radius * 0.24f, radius * 0.18f),
    )
    blush(center, radius, alpha, spread = 0.58f, height = 0.34f)
}

/** ЛИСА: острые ушки и светлая мордочка. */
fun DrawScope.drawFox(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val body = Palette.Fox.copy(alpha = alpha)
    val dark = Palette.FoxDark.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    pointedEars(center, radius, body, dark, Palette.Pink.copy(alpha = alpha))
    head(center, radius, body, dark)
    eyes(center, radius, blink, ink, spread = 0.36f, height = -0.20f)
    muzzle(center, radius, Palette.Cream.copy(alpha = alpha), ink)
    blush(center, radius, alpha, spread = 0.70f, height = 0.02f)
}

/** МИШКА: круглые ушки. */
fun DrawScope.drawBear(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val body = Palette.Bear.copy(alpha = alpha)
    val dark = Palette.BearDark.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    for (side in listOf(-1f, 1f)) {
        val ear = Offset(center.x + side * radius * 0.76f, center.y - radius * 0.78f)
        drawCircle(body, radius * 0.34f, ear)
        drawCircle(dark, radius * 0.34f, ear, style = Stroke(width = radius * 0.09f))
        drawCircle(Palette.Pink.copy(alpha = 0.5f * alpha), radius * 0.16f, ear)
    }
    head(center, radius, body, dark)
    eyes(center, radius, blink, ink, spread = 0.34f, height = -0.18f)
    muzzle(center, radius, Palette.Cream.copy(alpha = alpha), ink)
}

/** ВОЛК: серый, с клычками. */
fun DrawScope.drawWolf(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val body = Palette.Wolf.copy(alpha = alpha)
    val dark = Palette.WolfDark.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    pointedEars(center, radius, body, dark, Palette.Pink.copy(alpha = 0.7f * alpha))
    head(center, radius, body, dark)
    eyes(center, radius, blink, ink, spread = 0.36f, height = -0.22f)
    muzzle(center, radius, Palette.Cream.copy(alpha = alpha), ink)
    // Клычки
    for (side in listOf(-1f, 1f)) {
        val tooth = Path().apply {
            moveTo(center.x + side * radius * 0.16f, center.y + radius * 0.52f)
            lineTo(center.x + side * radius * 0.28f, center.y + radius * 0.52f)
            lineTo(center.x + side * radius * 0.22f, center.y + radius * 0.70f)
            close()
        }
        drawPath(tooth, Palette.Cream.copy(alpha = alpha))
    }
}

/** ЛЕВ: грива. */
fun DrawScope.drawLion(center: Offset, radius: Float, blink: Float = 1f, alpha: Float = 1f) {
    val mane = Palette.LionMane.copy(alpha = alpha)
    val body = Palette.Lion.copy(alpha = alpha)
    val ink = Palette.Ink.copy(alpha = alpha)

    for (i in 0 until 12) {
        val angle = (i / 12f) * 2f * PI.toFloat()
        drawCircle(
            color = mane,
            radius = radius * 0.34f,
            center = Offset(center.x + cos(angle) * radius * 0.98f, center.y + sin(angle) * radius * 0.98f),
        )
    }
    head(center, radius, body, mane)
    eyes(center, radius, blink, ink, spread = 0.34f, height = -0.16f)
    muzzle(center, radius, Palette.Cream.copy(alpha = alpha), ink)
    for (side in listOf(-1f, 1f)) {
        for (i in 0..1) {
            drawLine(
                color = mane,
                start = Offset(center.x + side * radius * 0.34f, center.y + radius * (0.34f + i * 0.14f)),
                end = Offset(center.x + side * radius * 0.92f, center.y + radius * (0.24f + i * 0.28f)),
                strokeWidth = radius * 0.06f,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Лицо-картинка: люди нарисованы заранее, поэтому просто кладём картинку в круг. */
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
 * Закрытые глаза для лиц-картинок: перекрываем зрачок цветом кожи и рисуем
 * дугу-ресничку. Моргание короткое (около 120 мс), поэтому закрываем глаза
 * целиком, без промежуточных кадров — так выходит аккуратная улыбка глазами.
 */
fun DrawScope.drawEyelids(
    eyes: EyeSpots,
    center: Offset,
    radius: Float,
    alpha: Float = 1f,
) {
    val side = radius * 2.30f
    val left = center.x - side / 2f
    val top = center.y - side / 2f
    val halfWidth = maxOf(eyes.width, 0.055f) * side * 0.75f
    val halfHeight = maxOf(eyes.height, 0.055f) * side * 0.72f
    val lid = eyes.skin.copy(alpha = alpha)
    val lash = Palette.Ink.copy(alpha = alpha)

    for (spot in listOf(eyes.leftX to eyes.leftY, eyes.rightX to eyes.rightY)) {
        val cx = left + spot.first * side
        val cy = top + spot.second * side
        drawOval(
            color = lid,
            topLeft = Offset(cx - halfWidth * 1.25f, cy - halfHeight * 1.45f),
            size = Size(halfWidth * 2.5f, halfHeight * 2.6f),
        )
        drawArc(
            color = lash,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - halfWidth, cy - halfHeight * 0.9f),
            size = Size(halfWidth * 2f, halfHeight * 2f),
            style = Stroke(width = side * 0.012f, cap = StrokeCap.Round),
        )
    }
}

// --- Общая точка входа ------------------------------------------------------

fun DrawScope.drawCharacter(
    character: Cast,
    center: Offset,
    radius: Float,
    blink: Float = 1f,
    alpha: Float = 1f,
    image: ImageBitmap? = null,
) {
    if (image != null) {
        drawFaceImage(image, center, radius, alpha)
        val eyes = character.eyes
        if (eyes != null && blink < 0.45f) drawEyelids(eyes, center, radius, alpha)
        return
    }
    when (character) {
        Cast.KIRA -> drawKira(center, radius, blink, alpha)
        Cast.MAMA -> drawMama(center, radius, blink, alpha)
        Cast.PAPA -> drawPapa(center, radius, blink, alpha)
        Cast.KRYA -> drawKrya(center, radius, blink, alpha)
        Cast.HEDGEHOG -> drawHedgehog(center, radius, blink, alpha)
        Cast.FOX -> drawFox(center, radius, blink, alpha)
        Cast.BEAR -> drawBear(center, radius, blink, alpha)
        Cast.WOLF -> drawWolf(center, radius, blink, alpha)
        Cast.LION -> drawLion(center, radius, blink, alpha)
    }
}

/** Настроение персонажа: от него зависит, как он себя ведёт. */
enum class Mood {
    /** Ждёт своей очереди: просто дышит и моргает. */
    IDLE,

    /** Его ход: подпрыгивает на месте, чтобы ребёнок видел, кого ждут. */
    ACTIVE,

    /** Выиграл: прыгает высоко и весело. */
    WINNER,

    /** Проиграл: вздыхает и качает головой — но не грустно. */
    LOSER,
}

/** Персонаж, который живёт сам по себе: дышит, моргает и реагирует на игру. */
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

    // Прыжки и вздохи: свой ритм на каждое настроение.
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
                drawCharacter(character, center, radius, blink, alpha, image)
            }
        }
    }
}

/** Плоская иконка персонажа без анимации — для счёта и кнопок. */
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
