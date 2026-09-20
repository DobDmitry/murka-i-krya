package com.dobdmitry.murkakrya.ui

/**
 * Надписи игры. Правила простые:
 *  - слово короткое, заглавными, без курсива;
 *  - у каждого есть [speech] — как это произносит голос;
 *  - рядом в интерфейсе всегда стоит иконка, дублирующая смысл.
 *
 * Имена героев живут в Cast.kt.
 */
data class Phrase(val text: String, val speech: String)

object Phrases {
    val AGAIN = Phrase("ЕЩЁ РАЗ", "Ещё раз")
    val SOUND = Phrase("ЗВУК", "Звук")
    val DRAW = Phrase("НИЧЬЯ", "Ничья. Оба молодцы")
    val CHAMPION = Phrase("ЧЕМПИОН", "Три победы подряд! Ты настоящий чемпион!")

    /** Голос иногда зовёт ходить — но не каждый раз, иначе надоест. */
    val NUDGES = listOf(
        "Твой ход",
        "Кира, ходи",
        "Давай, Кира",
        "Теперь ты",
    )

    /** Похвала после победы ребёнка. */
    val PRAISE = listOf(
        "Молодец!",
        "Здорово!",
        "Умница!",
        "Вот это да!",
    )

    /** Поддержка после проигрыша: проигрывать не обидно. */
    val SUPPORT = listOf(
        "Ничего, ещё разок",
        "Почти получилось",
        "Давай ещё раз",
        "В следующий раз повезёт",
    )

    fun name(character: Cast) = Phrase(character.display, character.speech)

    fun turn(character: Cast) = Phrase(character.turnText, character.turnSpeech)

    fun win(character: Cast) = Phrase("УРА, ${character.display}!", character.winSpeech)
}
