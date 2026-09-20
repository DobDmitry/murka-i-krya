package com.dobdmitry.murkakrya.ui

import androidx.compose.ui.graphics.Color
import kira.core.Difficulty

/** Живой соперник сидит рядом, компьютерным управляет игра. */
enum class Kind { HUMAN, COMPUTER }

/**
 * Все герои игры в одном месте.
 *
 * Хотите другое имя или нового героя — правите эту таблицу и кладёте
 * его лицо в art/, а потом запускаете `python3 tools/make_faces.py`.
 *
 * Слова короткие и заглавными, рядом всегда стоит лицо, поэтому играть
 * можно и не читая. Все компьютерные соперники играют одинаково легко:
 * ребёнку 4 лет важно выбрать, с кем играть, а не насколько трудно.
 */
enum class Cast(
    val display: String,
    val speech: String,
    val turnText: String,
    val turnSpeech: String,
    val winSpeech: String,
    val kind: Kind,
    val color: Color,
    val darkColor: Color,
) {
    // --- Семья: играют руками, по очереди на одном телефоне ---------------

    KIRA(
        display = "КИРА",
        speech = "Кира",
        turnText = "ХОД КИРЫ",
        turnSpeech = "Ход Киры",
        winSpeech = "Ура! Кира выиграла",
        kind = Kind.HUMAN,
        color = Palette.Pink,
        darkColor = Palette.PinkDark,
    ),
    MAMA(
        display = "МАМА",
        speech = "Мама",
        turnText = "ХОД МАМЫ",
        turnSpeech = "Ход мамы",
        winSpeech = "Ура! Мама выиграла",
        kind = Kind.HUMAN,
        color = Palette.Lilac,
        darkColor = Palette.LilacDark,
    ),
    PAPA(
        display = "ПАПА",
        speech = "Папа",
        turnText = "ХОД ПАПЫ",
        turnSpeech = "Ход папы",
        winSpeech = "Ура! Папа выиграл",
        kind = Kind.HUMAN,
        color = Palette.Sky,
        darkColor = Palette.SkyDark,
    ),

    // --- Соперники за компьютер -------------------------------------------

    ARTEM(
        display = "АРТЁМ",
        speech = "Артём",
        turnText = "ХОД АРТЁМА",
        turnSpeech = "Ход Артёма",
        winSpeech = "Ура! Артём выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Fox,
        darkColor = Palette.FoxDark,
    ),
    BABA_IRA(
        display = "БАБА ИРА",
        speech = "Баба Ира",
        turnText = "ХОД БАБЫ ИРЫ",
        turnSpeech = "Ход бабы Иры",
        winSpeech = "Ура! Баба Ира выиграла",
        kind = Kind.COMPUTER,
        color = Palette.Plum,
        darkColor = Palette.PlumDark,
    ),
    ROMA(
        display = "РОМА",
        speech = "Рома",
        turnText = "ХОД РОМЫ",
        turnSpeech = "Ход Ромы",
        winSpeech = "Ура! Рома выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Grass,
        darkColor = Palette.GrassDark,
    ),
    BABA_LUBA(
        display = "БАБА ЛЮБА",
        speech = "Баба Люба",
        turnText = "ХОД БАБЫ ЛЮБЫ",
        turnSpeech = "Ход бабы Любы",
        winSpeech = "Ура! Баба Люба выиграла",
        kind = Kind.COMPUTER,
        color = Palette.Mint,
        darkColor = Palette.MintDark,
    ),
    HABIB(
        display = "ХАБИБ",
        speech = "Хабиб",
        turnText = "ХОД ХАБИБА",
        turnSpeech = "Ход Хабиба",
        winSpeech = "Ура! Хабиб выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Wolf,
        darkColor = Palette.WolfDark,
    ),
    DEDA_MISHA(
        display = "ДЕДА МИША",
        speech = "Деда Миша",
        turnText = "ХОД ДЕДЫ МИШИ",
        turnSpeech = "Ход деды Миши",
        winSpeech = "Ура! Деда Миша выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Hedgehog,
        darkColor = Palette.HedgehogDark,
    ),
    UCHITEL(
        display = "УЧИТЕЛЬ",
        speech = "Учитель",
        turnText = "ХОД УЧИТЕЛЯ",
        turnSpeech = "Ход учителя",
        winSpeech = "Ура! Учитель выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Lion,
        darkColor = Palette.LionMane,
    ),
    MATVEY(
        display = "МАТВЕЙ",
        speech = "Матвей",
        turnText = "ХОД МАТВЕЯ",
        turnSpeech = "Ход Матвея",
        winSpeech = "Ура! Матвей выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Krya,
        darkColor = Palette.KryaDark,
    ),
    ILYA(
        display = "ИЛЬЯ",
        speech = "Илья",
        turnText = "ХОД ИЛЬИ",
        turnSpeech = "Ход Ильи",
        winSpeech = "Ура! Илья выиграл",
        kind = Kind.COMPUTER,
        color = Palette.Bear,
        darkColor = Palette.BearDark,
    );

    /**
     * Все компьютерные соперники играют на лёгком уровне: ходят почти наугад
     * и часто проигрывают, но не поддаются демонстративно.
     */
    val difficulty: Difficulty
        get() = Difficulty.EASY

    companion object {
        /** Кто всегда ходит первым. */
        val HERO: Cast = KIRA

        /** Живые соперники: игра один на один на одном телефоне. */
        val PEOPLE: List<Cast> = listOf(MAMA, PAPA)

        /** Соперники за компьютер. */
        val COMPUTER: List<Cast> = listOf(
            ARTEM, BABA_IRA, ROMA,
            BABA_LUBA, HABIB, DEDA_MISHA,
            UCHITEL, MATVEY, ILYA,
        )

        val DEFAULT_OPPONENT: Cast = ARTEM
    }
}
