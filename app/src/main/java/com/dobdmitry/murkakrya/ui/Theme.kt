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
    val Cream = Color(0xFFFFFBF2)

    // Люди
    val Skin = Color(0xFFFFD6B0)
    val SkinWarm = Color(0xFFF7C79B)
    val KiraHair = Color(0xFF8A5A34)
    val MamaHair = Color(0xFF5C3A22)
    val PapaHair = Color(0xFF4A3524)

    val Pink = Color(0xFFEE7FA0)
    val PinkDark = Color(0xFFC0506F)
    val Lilac = Color(0xFFA98CE8)
    val LilacDark = Color(0xFF7659B5)
    val Sky = Color(0xFF63B4E8)
    val SkyDark = Color(0xFF3A7FAE)

    // Звери
    val Krya = Color(0xFFF2C14E)
    val KryaDark = Color(0xFFA9781A)
    val Beak = Color(0xFFF2994A)
    val BeakDark = Color(0xFFB9682A)
    val Hedgehog = Color(0xFFD8B48C)
    val HedgehogDark = Color(0xFF6E5540)
    val Fox = Color(0xFFE8833A)
    val FoxDark = Color(0xFFA9531C)
    val Bear = Color(0xFFB08464)
    val BearDark = Color(0xFF7A5539)
    val Wolf = Color(0xFF9AA7B4)
    val WolfDark = Color(0xFF5E6B78)
    val Lion = Color(0xFFF0B429)
    val LionMane = Color(0xFFC9821F)

    val Mint = Color(0xFF6FCF97)

    /** Цвета конфетти и звёздочек. */
    val Confetti = listOf(Pink, Sky, Mint, Lilac, Krya, Fox)

    fun of(character: Cast): Color = character.color

    fun darkOf(character: Cast): Color = character.darkColor
}

private val ColorScheme = lightColorScheme(
    primary = Palette.Pink,
    onPrimary = Palette.Cream,
    secondary = Palette.Sky,
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
fun GameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = BigTypography,
        content = content,
    )
}
