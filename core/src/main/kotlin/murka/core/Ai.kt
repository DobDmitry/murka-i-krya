package murka.core

import kotlin.random.Random

/** Соперник-компьютер: по положению фигур выбирает клетку. */
interface Ai {
    /** Индекс клетки (0..8), которую займёт [me]. Всегда легальный ход: ИИ не пропускает. */
    fun chooseMove(board: Board, me: Player): Int
}

fun aiFor(difficulty: Difficulty, random: Random = Random.Default): Ai = when (difficulty) {
    Difficulty.KITTEN -> KittenAi(random)
    Difficulty.CAT -> CatAi(random)
    Difficulty.LION -> LionAi(random)
}

/**
 * КОТЁНОК — «ЛЕГКО».
 *
 * Проигрывает часто, но не поддаётся демонстративно:
 *  - никогда не пропускает ход;
 *  - не ставит фигуру вплотную к чужой паре, если решил её не блокировать
 *    (такой ход читается как нарочный промах);
 *  - свой выигрыш чаще всего забирает;
 *  - каждый ход старается выглядеть осмысленным — тянет собственную линию.
 */
class KittenAi(private val random: Random = Random.Default) : Ai {

    override fun chooseMove(board: Board, me: Player): Int {
        val free = board.emptyCells()
        require(free.isNotEmpty()) { "Ходить некуда" }
        if (free.size == 1) return free[0]

        val myWins = Rules.winningMoves(board, me)
        if (myWins.isNotEmpty() && random.nextDouble() < TAKE_WIN_CHANCE) {
            return myWins.random(random)
        }

        val threats = Rules.winningMoves(board, me.opponent)
        if (threats.isNotEmpty()) {
            if (random.nextDouble() < BLOCK_CHANCE) return threats.random(random)

            // Блокировать не стали — тогда уходим от чужой пары подальше,
            // чтобы ход не выглядел поддавками.
            val awkward = cellsAroundThreats(board, me.opponent)
            val calm = free.filterNot { it in awkward || it in threats }
            if (calm.isNotEmpty()) return purposefulMove(board, me, calm)

            // Уйти некуда — тогда уж честно блокируем, это естественнее промаха.
            return threats.random(random)
        }

        return purposefulMove(board, me, free)
    }

    /** Клетки самой угрозы и всё, что к ней вплотную примыкает. */
    private fun cellsAroundThreats(board: Board, opponent: Player): Set<Int> {
        val result = mutableSetOf<Int>()
        for (line in Rules.LINES) {
            val mine = line.count { board[it] == opponent }
            val free = line.filter { board.isEmpty(it) }
            if (mine == 2 && free.size == 1) {
                result += line
                for (cell in line) result += Rules.neighbours(cell)
            }
        }
        return result
    }

    /** Ход, который выглядит осмысленным: продолжает свою линию, иначе — наугад. */
    private fun purposefulMove(board: Board, me: Player, candidates: List<Int>): Int {
        if (random.nextDouble() < PURPOSEFUL_CHANCE) {
            val building = candidates.filter { cell ->
                val after = board.withMove(cell, me)
                Rules.LINES.any { line ->
                    cell in line &&
                        line.count { after[it] == me } == 2 &&
                        line.none { after[it] == me.opponent }
                }
            }
            if (building.isNotEmpty()) return building.random(random)
        }
        return candidates.random(random)
    }

    private companion object {
        /** Как часто котёнок забирает свой выигрыш. */
        const val TAKE_WIN_CHANCE = 0.5

        /** Как часто он замечает чужую угрозу. Редко — на то и «ЛЕГКО». */
        const val BLOCK_CHANCE = 0.12

        /** Как часто ход тянет собственную линию, а не просто занимает клетку. */
        const val PURPOSEFUL_CHANCE = 0.25
    }
}

/**
 * КОТ — «СРЕДНЕ».
 *
 * Забирает свой выигрыш, блокирует очевидную угрозу, дальше держится
 * простого порядка «центр → угол → край». Вилки не строит и от вилок не защищается.
 */
class CatAi(private val random: Random = Random.Default) : Ai {

    override fun chooseMove(board: Board, me: Player): Int {
        val free = board.emptyCells()
        require(free.isNotEmpty()) { "Ходить некуда" }

        Rules.winningMoves(board, me).takeIf { it.isNotEmpty() }?.let { return it.random(random) }
        Rules.winningMoves(board, me.opponent).takeIf { it.isNotEmpty() }
            ?.let { return it.random(random) }

        if (board.isEmpty(Rules.CENTER)) return Rules.CENTER
        Rules.CORNERS.filter { board.isEmpty(it) }.takeIf { it.isNotEmpty() }
            ?.let { return it.random(random) }
        return Rules.EDGES.filter { board.isEmpty(it) }.random(random)
    }
}

/**
 * ЛЕВ — «ТРУДНО».
 *
 * Полный минимакс с запоминанием позиций: выигрывает при любой ошибке соперника
 * и никогда не проигрывает. Быстрая победа ценится выше долгой, долгое
 * поражение — выше быстрого.
 */
class LionAi(private val random: Random = Random.Default) : Ai {

    /** Оценки позиций переиспользуются между ходами: партия просчитывается один раз. */
    private val memo = HashMap<String, Int>()

    override fun chooseMove(board: Board, me: Player): Int = bestMoves(board, me).random(random)

    /** Все одинаково сильные ходы. Публично — на этом держатся тесты. */
    fun bestMoves(board: Board, me: Player): List<Int> {
        val free = board.emptyCells()
        require(free.isNotEmpty()) { "Ходить некуда" }
        var best = Int.MIN_VALUE
        val result = mutableListOf<Int>()
        for (cell in free) {
            val score = score(board.withMove(cell, me), me, me.opponent, 1)
            if (score > best) {
                best = score
                result.clear()
                result += cell
            } else if (score == best) {
                result += cell
            }
        }
        return result
    }

    private fun score(
        board: Board,
        me: Player,
        turn: Player,
        depth: Int,
    ): Int {
        val key = board.code() + turn.name.first() + me.name.first()
        memo[key]?.let { return it }

        val value = when (val state = Rules.state(board)) {
            is GameState.Win -> if (state.winner == me) WIN - depth else depth - WIN
            GameState.Draw -> 0
            is GameState.Playing -> {
                val children = board.emptyCells().map { cell ->
                    score(board.withMove(cell, turn), me, turn.opponent, depth + 1)
                }
                if (turn == me) children.max() else children.min()
            }
        }
        memo[key] = value
        return value
    }

    private companion object {
        const val WIN = 100
    }
}
