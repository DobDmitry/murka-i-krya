package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Голос игры: любая надпись, появившись, проговаривается. */
val LocalSpeaker = staticCompositionLocalOf<(String) -> Unit> { {} }

/**
 * Надпись, которую озвучивают. Появилась на экране — прозвучала.
 * Ребёнок сопоставляет звучание с написанием.
 */
@Composable
fun SpokenLabel(
    phrase: Phrase,
    modifier: Modifier = Modifier,
    color: Color = Palette.Ink,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
    speak: Boolean = true,
) {
    val speaker = LocalSpeaker.current
    LaunchedEffect(phrase.text, speak) {
        if (speak) speaker(phrase.speech)
    }
    Text(
        text = phrase.text,
        style = style,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = modifier,
    )
}

/**
 * Большая кнопка: сверху иконка, под ней короткое слово.
 * Играть можно и не читая — иконка дублирует смысл.
 */
@Composable
fun BigButton(
    phrase: Phrase,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 96.dp,
    highlighted: Boolean = false,
    background: Color = Palette.Cream,
    speakLabel: Boolean = false,
    icon: @Composable (Modifier) -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "кнопка",
    )
    val pulse = rememberInfiniteTransition(label = "подсказка")
    val glow by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "подсветка",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale * (if (highlighted) 1f + glow * 0.04f else 1f)
                scaleY = scale * (if (highlighted) 1f + glow * 0.04f else 1f)
            }
            .background(
                color = if (highlighted) Palette.BackgroundDeep else background,
                shape = RoundedCornerShape(28.dp),
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() },
                )
            },
    ) {
        icon(Modifier.size(iconSize))
        SpokenLabel(phrase = phrase, speak = speakLabel)
    }
}

/**
 * Кнопка «подержи и получится»: выход из игры возможен только так.
 * Кольцо вокруг иконки показывает, сколько осталось держать.
 */
@Composable
fun HoldButton(
    onHold: () -> Unit,
    modifier: Modifier = Modifier,
    holdMillis: Int = 1500,
    ringColor: Color = Palette.Pink,
    icon: @Composable (Modifier) -> Unit,
) {
    val progress = remember { Animatable(0f) }
    var holding by remember { mutableStateOf(false) }

    LaunchedEffect(holding) {
        if (holding) {
            progress.animateTo(1f, tween(durationMillis = holdMillis, easing = LinearEasing))
            if (progress.value >= 1f) {
                holding = false
                progress.snapTo(0f)
                onHold()
            }
        } else {
            progress.animateTo(0f, tween(durationMillis = 200))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    holding = true
                    tryAwaitRelease()
                    holding = false
                }
            )
        },
    ) {
        icon(Modifier.fillMaxSize().padding(10.dp))
        if (progress.value > 0f) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = size.minDimension * 0.09f
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.value,
                    useCenter = false,
                    topLeft = Offset(stroke / 2f, stroke / 2f),
                    size = Size(size.width - stroke, size.height - stroke),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
    }
}

/** Ряд звёздочек-побед: выиграл — прилетела звезда, последняя прилетает с отскоком. */
@Composable
fun StarRow(count: Int, maxCount: Int = 9, modifier: Modifier = Modifier, color: Color = Palette.Krya) {
    val arrival = remember { Animatable(1f) }
    LaunchedEffect(count) {
        if (count > 0) {
            arrival.snapTo(0f)
            arrival.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow),
            )
        }
    }
    Canvas(modifier) {
        val step = size.width / maxCount
        val radius = (step * 0.42f).coerceAtMost(size.height * 0.45f)
        for (i in 0 until maxCount) {
            val fresh = i == count - 1
            val grow = if (fresh) arrival.value else 1f
            val center = Offset(
                step * (i + 0.5f),
                size.height / 2f - (1f - grow) * size.height * 1.4f,
            )
            drawStar(center, radius * (0.4f + 0.6f * grow), if (i < count) color else Palette.Pencil, filled = i < count)
        }
    }
}

/** Мягкая пульсация для того, чей сейчас ход. */
@Composable
fun rememberPulse(active: Boolean, period: Int = 700): Float {
    val transition = rememberInfiniteTransition(label = "пульс")
    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(period), RepeatMode.Reverse),
        label = "пульс",
    )
    return if (active) value else 0f
}

/** Фон с лёгким тёплым пятном по центру — чтобы экран не был плоским. */
fun DrawScope.drawWarmBackground() {
    drawRect(Palette.Background)
    drawCircle(
        color = Palette.BackgroundDeep.copy(alpha = 0.55f),
        radius = size.minDimension * 0.75f,
        center = Offset(size.width / 2f, size.height * 0.42f),
    )
}
