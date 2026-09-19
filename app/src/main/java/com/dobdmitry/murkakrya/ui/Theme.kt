package com.dobdmitry.murkakrya.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Палитра тёплая и пастельная: фон кремовый, персонажи контрастные.
 * Кислотных цветов нет — на них быстро устают глаза.
 */
object Palette {
    val Background = Color(0xFFFFF4E2)
    val BackgroundDeep = Color(0xFFFFE6C4)
    val Ink = Color(0xFF5B4436)
    val Pencil = Color(0xFF8A6647)

    val Murka = Color(0xFFF2994A)
    val MurkaDark = Color(0xFFB9682A)
    val Krya = Color(0xFFF2C14E)
    val KryaDark = Color(0xFFA9781A)

    val Pink = Color(0xFFEE7FA0)
    val Sky = Color(0xFF63B4E8)
    val Mint = Color(0xFF6FCF97)
    val Lilac = Color(0xFFA98CE8)
    val Cream = Color(0xFFFFFBF2)

    /** Цвета конфетти и звёздочек. */
    val Confetti = listOf(Pink, Sky, Mint, Lilac, Murka, Krya)

    fun of(player: murka.core.Player): Color =
        if (player == murka.core.Player.MURKA) Murka else Krya

    fun darkOf(player: murka.core.Player): Color =
        if (player == murka.core.Player.MURKA) MurkaDark else KryaDark
}

private val ColorScheme = lightColorScheme(
    primary = Palette.Murka,
    onPrimary = Palette.Cream,
    secondary = Palette.Krya,
    onSecondary = Palette.Ink,
    background = Palette.Background,
    onBackground = Palette.Ink,
    surface = Palette.Cream,
    onSurface = Palette.Ink,
)

/**
 * Шрифт простой, без засечек, очень жирный и очень крупный.
 * Курсива нет нигде: четырёхлетке его читать тяжело.
 */
private fun big(size: Int, weight: FontWeight = FontWeight.Black) = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = weight,
    fontStyle = FontStyle.Normal,
    fontSize = size.sp,
    letterSpacing = 0.5.sp,
)

private val BigTypography = Typography(
    displayLarge = big(46),
    displayMedium = big(38),
    headlineLarge = big(34),
    headlineMedium = big(30),
    titleLarge = big(28),
    bodyLarge = big(28),
    labelLarge = big(28),
)

@Composable
fun MurkaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = BigTypography,
        content = content,
    )
}
