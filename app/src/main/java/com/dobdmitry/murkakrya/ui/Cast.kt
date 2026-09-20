package com.dobdmitry.murkakrya.ui

import androidx.compose.ui.graphics.Color
import kira.core.Difficulty

/** Человек или зверь: люди играют руками, звери — компьютером. */
enum class Kind { HUMAN, ANIMAL }

/**
 * Где на картинке лица находятся глаза — чтобы игра умела их закрывать,
 * когда персонаж моргает. Доли от стороны картинки.
 *
 * Значения считает скрипт tools/find_eyes.py: он же кладёт в art/preview
 * картинки с закрытыми глазами, на которых видно, попали веки или нет.
 */
data class EyeSpots(
    val leftX: Float,
    val leftY: Float,
    val rightX: Float,
    val rightY: Float,
    val width: Float,
    val height: Float,
    val skin: Color,
)

/**
 * Все герои игры в одном месте.
 *
 * Хотите другое имя или другого героя — правите только эту таблицу
 * и рисующую функцию в Characters.kt. Слова короткие и заглавными,
 * рядом с каждым всегда стоит его картинка.
 *
 * [difficulty] есть только у компьютерных соперников: чем сильнее зверь,
 * тем больше звёздочек под его картинкой.
 */
enum class Cast(
    val display: String,
    val speech: String,
    val turnText: String,
    val turnSpeech: String,
    val winSpeech: String,
    val kind: Kind,
    val difficulty: Difficulty?,
    val color: Color,
    val darkColor: Color,
    val eyes: EyeSpots? = null,
) {
    KIRA(
        display = "КИРА",
        speech = "Кира",
        turnText = "ХОД КИРЫ",
        turnSpeech = "Ход Киры",
        winSpeech = "Ура! Кира выиграла",
        kind = Kind.HUMAN,
        difficulty = null,
        color = Palette.Pink,
        darkColor = Palette.PinkDark,
        eyes = EyeSpots(0.336f, 0.588f, 0.639f, 0.617f, 0.123f, 0.100f, Color(0xFFFDDCB9)),
    ),
    MAMA(
        display = "МАМА",
        speech = "Мама",
        turnText = "ХОД МАМЫ",
        turnSpeech = "Ход мамы",
        winSpeech = "Ура! Мама выиграла",
        kind = Kind.HUMAN,
        difficulty = null,
        color = Palette.Lilac,
        darkColor = Palette.LilacDark,
        eyes = EyeSpots(0.327f, 0.502f, 0.636f, 0.531f, 0.133f, 0.107f, Color(0xFFFAD8B3)),
    ),
    PAPA(
        display = "ПАПА",
        speech = "Папа",
        turnText = "ХОД ПАПЫ",
        turnSpeech = "Ход папы",
        winSpeech = "Ура! Папа выиграл",
        kind = Kind.HUMAN,
        difficulty = null,
        color = Palette.Sky,
        darkColor = Palette.SkyDark,
        eyes = EyeSpots(0.306f, 0.568f, 0.671f, 0.564f, 0.145f, 0.131f, Color(0xFFFBD9B4)),
    ),
    KRYA(
        display = "КРЯ",
        speech = "Кря",
        turnText = "ХОД КРЯ",
        turnSpeech = "Ход Кря",
        winSpeech = "Ура! Кря выиграл",
        kind = Kind.ANIMAL,
        difficulty = Difficulty.EASY,
        color = Palette.Krya,
        darkColor = Palette.KryaDark,
    ),
    HEDGEHOG(
        display = "ЁЖИК",
        speech = "Ёжик",
        turnText = "ХОД ЁЖИКА",
        turnSpeech = "Ход ёжика",
        winSpeech = "Ура! Ёжик выиграл",
        kind = Kind.ANIMAL,
        difficulty = Difficulty.EASY,
        color = Palette.Hedgehog,
        darkColor = Palette.HedgehogDark,
    ),
    FOX(
        display = "ЛИСА",
        speech = "Лиса",
        turnText = "ХОД ЛИСЫ",
        turnSpeech = "Ход лисы",
        winSpeech = "Ура! Лиса выиграла",
        kind = Kind.ANIMAL,
        difficulty = Difficulty.MEDIUM,
        color = Palette.Fox,
        darkColor = Palette.FoxDark,
    ),
    BEAR(
        display = "МИШКА",
        speech = "Мишка",
        turnText = "ХОД МИШКИ",
        turnSpeech = "Ход мишки",
        winSpeech = "Ура! Мишка выиграл",
        kind = Kind.ANIMAL,
        difficulty = Difficulty.MEDIUM,
        color = Palette.Bear,
        darkColor = Palette.BearDark,
    ),
    WOLF(
        display = "ВОЛК",
        speech = "Волк",
        turnText = "ХОД ВОЛКА",
        turnSpeech = "Ход волка",
        winSpeech = "Ура! Волк выиграл",
        kind = Kind.ANIMAL,
        difficulty = Difficulty.HARD,
        color = Palette.Wolf,
        darkColor = Palette.WolfDark,
    ),
    LION(
        display = "ЛЕВ",
        speech = "Лев",
        turnText = "ХОД ЛЬВА",
        turnSpeech = "Ход льва",
        winSpeech = "Ура! Лев выиграл",
        kind = Kind.ANIMAL,
        difficulty = Difficulty.HARD,
        color = Palette.Lion,
        darkColor = Palette.LionMane,
    );

    /** Сколько звёздочек силы рисовать под картинкой: 1 — легко, 3 — трудно. */
    val strength: Int
        get() = when (difficulty) {
            Difficulty.EASY -> 1
            Difficulty.MEDIUM -> 2
            Difficulty.HARD -> 3
            null -> 0
        }

    companion object {
        /** Кто всегда ходит первым. */
        val HERO: Cast = KIRA

        /** Живые соперники: игра один на один на одном телефоне. */
        val PEOPLE: List<Cast> = listOf(MAMA, PAPA)

        /** Компьютерные соперники, от самого слабого к самому сильному. */
        val ANIMALS: List<Cast> = listOf(KRYA, HEDGEHOG, FOX, BEAR, WOLF, LION)

        val DEFAULT_OPPONENT: Cast = KRYA
    }
}
