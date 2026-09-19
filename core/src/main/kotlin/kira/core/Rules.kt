package kira.core

/** Правила игры: восемь линий, победа, ничья. Чистый Kotlin, без Android. */
object Rules {

    /** Все восемь выигрышных линий: три ряда, три столбца, две диагонали. */
    val LINES: List<List<Int>> = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6),
    )

    /** Кто ходит на пустом поле. Первой всегда начинает Кира. */
    val FIRST_PLAYER: Side = Side.FIRST

    /** Чей ход при текущем положении фигур (без учёта того, закончена ли партия). */
    fun turnFor(board: Board): Side =
        if (board.moveCount % 2 == 0) FIRST_PLAYER else FIRST_PLAYER.opponent

    /** Состояние партии по положению фигур. */
    fun state(board: Board): GameState {
        for (line in LINES) {
            val first = board[line[0]] ?: continue
            if (board[line[1]] == first && board[line[2]] == first) {
                return GameState.Win(first, line)
            }
        }
        return if (board.isFull) GameState.Draw else GameState.Playing(turnFor(board))
    }

    /** Победная линия, если она уже собрана. */
    fun winningLine(board: Board): List<Int>? = (state(board) as? GameState.Win)?.line

    /** Клетки, заняв которые [player] выигрывает прямо сейчас. */
    fun winningMoves(board: Board, player: Side): List<Int> =
        board.emptyCells().filter { cell ->
            val after = board.withMove(cell, player)
            (state(after) as? GameState.Win)?.winner == player
        }

    /**
     * Ходы, после которых у [player] появляется сразу две угрозы (вилка).
     * Используется ИИ и тестами.
     */
    fun forkMoves(board: Board, player: Side): List<Int> =
        board.emptyCells().filter { cell ->
            val after = board.withMove(cell, player)
            state(after) is GameState.Playing && winningMoves(after, player).size >= 2
        }

    /** Соседние клетки (включая диагональные) — нужны лёгкому уровню ИИ. */
    fun neighbours(cell: Int): List<Int> {
        val row = cell / 3
        val column = cell % 3
        val result = mutableListOf<Int>()
        for (dRow in -1..1) {
            for (dColumn in -1..1) {
                if (dRow == 0 && dColumn == 0) continue
                val r = row + dRow
                val c = column + dColumn
                if (r in 0..2 && c in 0..2) result += r * 3 + c
            }
        }
        return result
    }

    val CENTER: Int = 4
    val CORNERS: List<Int> = listOf(0, 2, 6, 8)
    val EDGES: List<Int> = listOf(1, 3, 5, 7)
}
