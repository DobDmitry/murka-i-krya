package murka.core

/** Чем закончилась (или не закончилась) партия. */
sealed class GameState {
    /** Партия продолжается, ходит [turn]. */
    data class Playing(val turn: Player) : GameState()

    /** Победа: [winner] собрал линию [line] из трёх клеток. */
    data class Win(val winner: Player, val line: List<Int>) : GameState()

    /** Ничья: поле заполнено, линии нет. */
    object Draw : GameState()

    val isOver: Boolean
        get() = this !is Playing
}
