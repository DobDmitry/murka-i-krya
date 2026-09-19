package kira.core

/** Чем закончилась (или не закончилась) партия. */
sealed class GameState {
    /** Партия продолжается, ходит [turn]. */
    data class Playing(val turn: Side) : GameState()

    /** Победа: [winner] собрал линию [line] из трёх клеток. */
    data class Win(val winner: Side, val line: List<Int>) : GameState()

    /** Ничья: поле заполнено, линии нет. */
    object Draw : GameState()

    val isOver: Boolean
        get() = this !is Playing
}
