package com.dobdmitry.murkakrya.ui

import murka.core.Difficulty
import murka.core.Player

/**
 * Все надписи игры. Правила простые:
 *  - слово короткое, заглавными, без курсива;
 *  - у каждого есть [speech] — как это произносит голос;
 *  - рядом в интерфейсе всегда стоит иконка, дублирующая смысл.
 *
 * Хотите другие имена персонажей — меняете только этот файл.
 */
data class Phrase(val text: String, val speech: String)

object Phrases {
    val MURKA = Phrase("МУРКА", "Мурка")
    val KRYA = Phrase("КРЯ", "Кря")

    val TOGETHER = Phrase("ВДВОЁМ", "Играем вдвоём")
    val ROBOT = Phrase("РОБОТ", "Играем с роботом")

    val EASY = Phrase("ЛЕГКО", "Легко")
    val MEDIUM = Phrase("СРЕДНЕ", "Средне")
    val HARD = Phrase("ТРУДНО", "Трудно")

    val AGAIN = Phrase("ЕЩЁ РАЗ", "Ещё раз")
    val SOUND = Phrase("ЗВУК", "Звук")
    val DRAW = Phrase("НИЧЬЯ", "Ничья. Оба молодцы")

    fun name(player: Player): Phrase = if (player == Player.MURKA) MURKA else KRYA

    fun turn(player: Player): Phrase =
        if (player == Player.MURKA) Phrase("ХОД МУРКИ", "Ход Мурки")
        else Phrase("ХОД КРЯ", "Ход Кря")

    fun win(player: Player): Phrase =
        if (player == Player.MURKA) Phrase("УРА, МУРКА!", "Ура! Мурка выиграла")
        else Phrase("УРА, КРЯ!", "Ура! Кря выиграл")

    fun level(difficulty: Difficulty): Phrase = when (difficulty) {
        Difficulty.KITTEN -> EASY
        Difficulty.CAT -> MEDIUM
        Difficulty.LION -> HARD
    }
}
