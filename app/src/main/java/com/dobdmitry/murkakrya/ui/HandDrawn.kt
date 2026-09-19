package com.dobdmitry.murkakrya.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Линии «от руки»: ровных в игре нет ни одной.
 * Дрожание считается от seed, поэтому в пределах партии линии не «кипят»,
 * а в новой партии рисуются заново и чуть иначе.
 */
fun wobblyPath(
    from: Offset,
    to: Offset,
    seed: Int,
    wobble: Float,
    segments: Int = 14,
): Path {
    val random = Random(seed)
    val length = hypot(to.x - from.x, to.y - from.y)
    if (length < 0.001f) return Path().apply { moveTo(from.x, from.y) }
    val dirX = (to.x - from.x) / length
    val dirY = (to.y - from.y) / length
    val normalX = -dirY
    val normalY = dirX
    val path = Path()
    for (i in 0..segments) {
        val t = i / segments.toFloat()
        // По краям линия прижимается к нужной точке, в середине гуляет сильнее.
        val edge = 1f - (2f * t - 1f) * (2f * t - 1f)
        val shift = (random.nextFloat() - 0.5f) * 2f * wobble * edge
        val x = from.x + dirX * length * t + normalX * shift
        val y = from.y + dirY * length * t + normalY * shift
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    return path
}

fun DrawScope.drawWobblyLine(
    from: Offset,
    to: Offset,
    color: Color,
    width: Float,
    seed: Int,
    wobble: Float,
    alpha: Float = 1f,
) {
    // Два прохода чуть разной толщины — получается след фломастера.
    drawPath(
        wobblyPath(from, to, seed, wobble),
        color.copy(alpha = 0.35f * alpha),
        style = Stroke(width = width * 1.45f, cap = StrokeCap.Round),
    )
    drawPath(
        wobblyPath(from, to, seed + 1, wobble * 0.7f),
        color.copy(alpha = alpha),
        style = Stroke(width = width, cap = StrokeCap.Round),
    )
}
