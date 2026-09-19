package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import murka.core.Board
import murka.core.Player

/**
 * Поле 3x3 во весь экран: линии нарисованы от руки, ячейки огромные,
 * промахнуться пальцем невозможно.
 */
@Composable
fun BoardView(
    board: Board,
    winLine: List<Int>?,
    winner: Player?,
    eraseProgress: Float,
    enabled: Boolean,
    roundSeed: Int,
    onCellTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val winProgress = remember { Animatable(0f) }
    LaunchedEffect(winLine, roundSeed) {
        if (winLine != null) {
            winProgress.snapTo(0f)
            winProgress.animateTo(1f, tween(durationMillis = 400, easing = FastOutSlowInEasing))
        } else {
            winProgress.snapTo(0f)
        }
    }

    Box(modifier.aspectRatio(1f)) {
        Canvas(Modifier.fillMaxSize()) {
            val cell = size.minDimension / 3f
            val wobble = cell * 0.045f
            val pad = cell * 0.12f
            for (i in 1..2) {
                drawWobblyLine(
                    from = Offset(cell * i, pad),
                    to = Offset(cell * i, size.height - pad),
                    color = Palette.Pencil,
                    width = cell * 0.055f,
                    seed = roundSeed * 31 + i,
                    wobble = wobble,
                )
                drawWobblyLine(
                    from = Offset(pad, cell * i),
                    to = Offset(size.width - pad, cell * i),
                    color = Palette.Pencil,
                    width = cell * 0.055f,
                    seed = roundSeed * 31 + 10 + i,
                    wobble = wobble,
                )
            }
        }

        Column(Modifier.fillMaxSize()) {
            for (row in 0..2) {
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    for (column in 0..2) {
                        val index = row * 3 + column
                        Cell(
                            index = index,
                            player = board[index],
                            winning = winLine?.contains(index) == true,
                            enabled = enabled && board.isEmpty(index),
                            alpha = eraseAlpha(column, eraseProgress),
                            roundSeed = roundSeed,
                            onTap = { onCellTap(index) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp),
                        )
                    }
                }
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            if (winLine != null && winner != null && winProgress.value > 0f) {
                drawWinLine(winLine, winner, winProgress.value, roundSeed)
            }
            if (eraseProgress > 0f && eraseProgress < 1f) {
                drawEraser(eraseProgress)
            }
        }
    }
}

/** Пока ластик не дошёл до столбца — фигуры на месте, после — исчезли. */
private fun eraseAlpha(column: Int, progress: Float): Float {
    if (progress <= 0f) return 1f
    val start = column / 3f * 0.72f
    val end = start + 0.26f
    return 1f - ((progress - start) / (end - start)).coerceIn(0f, 1f)
}

private fun DrawScope.drawWinLine(line: List<Int>, winner: Player, progress: Float, seed: Int) {
    val cell = size.minDimension / 3f
    fun center(index: Int) = Offset((index % 3 + 0.5f) * cell, (index / 3 + 0.5f) * cell)
    val from = center(line.first())
    val to = center(line.last())
    val direction = Offset(to.x - from.x, to.y - from.y)
    val length = kotlin.math.hypot(direction.x, direction.y)
    val extra = if (length > 0f) Offset(direction.x / length * cell * 0.28f, direction.y / length * cell * 0.28f) else Offset.Zero
    val full = wobblyPath(
        from = Offset(from.x - extra.x, from.y - extra.y),
        to = Offset(to.x + extra.x, to.y + extra.y),
        seed = seed * 97 + 5,
        wobble = cell * 0.05f,
    )
    val measure = PathMeasure()
    measure.setPath(full, false)
    val segment = Path()
    measure.getSegment(0f, measure.length * progress, segment, true)
    drawPath(
        segment,
        Palette.darkOf(winner).copy(alpha = 0.35f),
        style = Stroke(width = cell * 0.26f, cap = StrokeCap.Round),
    )
    drawPath(
        segment,
        Palette.of(winner),
        style = Stroke(width = cell * 0.16f, cap = StrokeCap.Round),
    )
}

/** Новая партия: поле стирается ластиком слева направо. */
private fun DrawScope.drawEraser(progress: Float) {
    val cell = size.minDimension / 3f
    val x = progress * (size.width + cell)
    drawRect(
        color = Palette.Background,
        topLeft = Offset.Zero,
        size = Size(x.coerceAtMost(size.width), size.height),
    )
    if (x < size.width + cell * 0.5f) {
        val width = cell * 0.55f
        val height = cell * 0.95f
        drawRoundRect(
            color = Palette.Pink,
            topLeft = Offset(x - width / 2f, size.height / 2f - height / 2f),
            size = Size(width, height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.25f),
        )
        drawRoundRect(
            color = Palette.Cream,
            topLeft = Offset(x - width / 2f, size.height / 2f - height / 2f),
            size = Size(width, height * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width * 0.25f),
        )
    }
}

@Composable
private fun Cell(
    index: Int,
    player: Player?,
    winning: Boolean,
    enabled: Boolean,
    alpha: Float,
    roundSeed: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    val press by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "нажатие",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = press
                scaleY = press
            }
            .pointerInput(enabled, index, roundSeed) {
                detectTapGestures(
                    onPress = {
                        if (enabled) {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        }
                    },
                    onTap = { if (enabled) onTap() },
                )
            }
    ) {
        if (player != null) {
            Piece(
                player = player,
                index = index,
                winning = winning,
                alpha = alpha,
                roundSeed = roundSeed,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/**
 * Фигура прыгает сверху с отскоком, слегка поворачивается,
 * поднимает пыль от удара, а потом просто живёт: дышит и моргает.
 */
@Composable
private fun Piece(
    player: Player,
    index: Int,
    winning: Boolean,
    alpha: Float,
    roundSeed: Int,
    modifier: Modifier = Modifier,
) {
    val drop = remember(player, index, roundSeed) { Animatable(1f) }
    val dust = remember(player, index, roundSeed) { Animatable(0f) }

    LaunchedEffect(player, index, roundSeed) {
        drop.snapTo(1f)
        dust.snapTo(0f)
        launch {
            delay(150)
            dust.animateTo(1f, tween(durationMillis = 430))
        }
        drop.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow),
        )
    }

    val jumpTransition = rememberInfiniteTransition(label = "победный прыжок")
    val jumpRaw by jumpTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "прыжок",
    )
    val jump = if (winning) jumpRaw else 0f

    Box(modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = -drop.value * size.height * 1.25f - jump * size.height * 0.14f
                    rotationZ = drop.value * 18f - jump * 6f
                    scaleX = 1f - drop.value * 0.08f
                    scaleY = 1f + drop.value * 0.10f
                    this.alpha = alpha
                }
        ) {
            LivingCharacter(
                player = player,
                modifier = Modifier.fillMaxSize(),
                seed = index % 3,
            )
        }
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.30f
            drawDust(
                center = Offset(size.width / 2f, size.height / 2f),
                radius = radius,
                progress = dust.value,
                color = Palette.Pencil.copy(alpha = alpha),
            )
        }
    }
}
