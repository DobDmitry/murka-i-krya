package murka.core

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * КОТЁНОК — уровень «ЛЕГКО».
 *
 * Требование простое и не совсем обычное: проигрывать часто, но не поддаваться
 * демонстративно. Поэтому проверяем и статистику, и «приличность» каждого хода.
 */
class KittenAiTest {

    /**
     * Модель четырёхлетки: свой выигрыш видит всегда, чужую угрозу замечает
     * примерно в половине случаев, в остальном ходит наугад.
     */
    private fun childMove(board: Board, me: Player, random: Random): Int {
        Rules.winningMoves(board, me).takeIf { it.isNotEmpty() }?.let { return it.random(random) }
        if (random.nextDouble() < 0.5) {
            Rules.winningMoves(board, me.opponent).takeIf { it.isNotEmpty() }
                ?.let { return it.random(random) }
        }
        return board.emptyCells().random(random)
    }

    private fun playGames(count: Int, seed: Int): Triple<Int, Int, Int> {
        val random = Random(seed)
        val kitten = KittenAi(random)
        val kittenSide = Player.KRYA
        var childWins = 0
        var kittenWins = 0
        var draws = 0
        repeat(count) {
            var board = Board.empty()
            var turn = Rules.FIRST_PLAYER
            while (Rules.state(board) is GameState.Playing) {
                val move =
                    if (turn == kittenSide) kitten.chooseMove(board, turn)
                    else childMove(board, turn, random)
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
            when (val state = Rules.state(board)) {
                is GameState.Win -> if (state.winner == kittenSide) kittenWins++ else childWins++
                else -> draws++
            }
        }
        return Triple(childWins, kittenWins, draws)
    }

    @Test
    fun `ребёнок выигрывает у котёнка часто`() {
        val (childWins, _, _) = playGames(2000, seed = 11)
        val share = childWins / 2000.0
        assertTrue("Ребёнок выиграл лишь ${(share * 100).toInt()}% партий", share > 0.50)
    }

    @Test
    fun `но котёнок всё-таки иногда выигрывает — это не поддавки`() {
        val (_, kittenWins, _) = playGames(2000, seed = 12)
        val share = kittenWins / 2000.0
        assertTrue("Котёнок выиграл ${(share * 100).toInt()}% — слишком мало", share > 0.10)
        assertTrue("Котёнок выиграл ${(share * 100).toInt()}% — слишком много", share < 0.33)
    }

    @Test
    fun `никогда не пропускает ход и не лезет в занятую клетку`() {
        val random = Random(5)
        val kitten = KittenAi(random)
        repeat(500) {
            var board = Board.empty()
            var turn = Rules.FIRST_PLAYER
            while (Rules.state(board) is GameState.Playing) {
                val move =
                    if (turn == Player.KRYA) kitten.chooseMove(board, turn)
                    else board.emptyCells().random(random)
                assertTrue("Клетка $move уже занята: $board", move in board.emptyCells())
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
        }
    }

    @Test
    fun `не поддаётся демонстративно — не ставит фигуру вплотную к чужой паре`() {
        val random = Random(9)
        val kitten = KittenAi(random)
        var checked = 0
        repeat(1500) {
            var board = Board.empty()
            var turn = Rules.FIRST_PLAYER
            while (Rules.state(board) is GameState.Playing) {
                val move: Int
                if (turn == Player.KRYA) {
                    val threats = Rules.winningMoves(board, Player.MURKA)
                    val ownWins = Rules.winningMoves(board, Player.KRYA)
                    move = kitten.chooseMove(board, turn)
                    if (threats.isNotEmpty() && move !in threats && move !in ownWins) {
                        val awkward = awkwardCells(board, Player.MURKA)
                        val escape = board.emptyCells().filterNot { it in awkward || it in threats }
                        if (escape.isNotEmpty()) {
                            checked++
                            assertTrue(
                                "Промах вплотную к угрозе выглядит поддавками: ход $move, поле $board",
                                move !in awkward,
                            )
                        }
                    }
                } else {
                    move = board.emptyCells().random(random)
                }
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
        }
        assertTrue("Проверка ни разу не сработала — тест бесполезен", checked > 100)
    }

    /** Клетки самой угрозы и всё, что примыкает к ней вплотную. */
    private fun awkwardCells(board: Board, opponent: Player): Set<Int> {
        val result = mutableSetOf<Int>()
        for (line in Rules.LINES) {
            if (line.count { board[it] == opponent } == 2 && line.count { board.isEmpty(it) } == 1) {
                result += line
                for (cell in line) result += Rules.neighbours(cell)
            }
        }
        return result
    }
}
