package kira.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Трудный уровень. Проверяем, что минимакс действительно идеален. */
class PerfectAiTest {

    private fun perfect(seed: Int = 1) = PerfectAi(Random(seed))

    @Test
    fun `забирает выигрыш немедленно`() {
        // Второй стоит на 0 и 1, первый грозит из 3-4. Выигрыш важнее защиты.
        val board = Board.of("BB.AA...A")
        assertEquals(listOf(2), perfect().bestMoves(board, Side.SECOND))
    }

    @Test
    fun `блокирует чужой выигрыш, когда своего нет`() {
        // Первый собрал 0-1, спасает только клетка 2.
        val board = Board.of("AA..B....")
        assertEquals(listOf(2), perfect().bestMoves(board, Side.SECOND))
    }

    @Test
    fun `свой выигрыш важнее чужой угрозы`() {
        val board = Board.of("AA.BB.A..")
        assertEquals(listOf(5), perfect().bestMoves(board, Side.SECOND))
    }

    @Test
    fun `наказывает ошибку соперника и доводит до победы`() {
        // Первый пошёл в угол, второй ответил углом вместо центра — это проигрыш.
        val mistake = Board.of("A.B......")
        val best = perfect().bestMoves(mistake, Side.FIRST)
        assertTrue("Идеальный соперник обязан найти выигрывающий план", best.isNotEmpty())
        for (move in best) {
            val after = mistake.withMove(move, Side.FIRST)
            assertEquals(
                "Ход $move не доводит до победы: $after",
                Outcome.WIN,
                PerfectPlay.outcome(after, forPlayer = Side.FIRST),
            )
        }
    }

    @Test
    fun `идеальный соперник не проигрывает — полный перебор за обе стороны`() {
        for (perfectSide in Side.values()) {
            playOut(Board.empty(), Rules.FIRST_PLAYER, perfectSide, perfect(), HashSet())
        }
    }

    @Test
    fun `идеальный против идеального — всегда ничья`() {
        val ai = perfect()
        val visited = HashSet<String>()
        fun walk(board: Board, turn: Side) {
            when (Rules.state(board)) {
                is GameState.Win -> throw AssertionError("Идеальная игра не должна давать победу: $board")
                GameState.Draw -> return
                is GameState.Playing -> {
                    if (!visited.add(board.code() + turn.name)) return
                    for (move in ai.bestMoves(board, turn)) {
                        walk(board.withMove(move, turn), turn.opponent)
                    }
                }
            }
        }
        walk(Board.empty(), Rules.FIRST_PLAYER)
    }

    @Test
    fun `случайного соперника идеальный наказывает почти всегда`() {
        val random = Random(7)
        val ai = PerfectAi(random)
        val perfectSide = Side.SECOND
        var perfectWins = 0
        var draws = 0
        repeat(200) {
            var board = Board.empty()
            var turn = Rules.FIRST_PLAYER
            while (Rules.state(board) is GameState.Playing) {
                val move =
                    if (turn == perfectSide) ai.chooseMove(board, perfectSide)
                    else board.emptyCells().random(random)
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
            when (val state = Rules.state(board)) {
                is GameState.Win -> {
                    assertEquals("Идеальный соперник проиграл случайному: $board", perfectSide, state.winner)
                    perfectWins++
                }
                else -> draws++
            }
        }
        assertEquals(200, perfectWins + draws)
        assertTrue("Идеальный выиграл лишь $perfectWins из 200", perfectWins > 120)
    }

    /** Перебираем все ответы соперника и все равноценные ходы идеального ИИ. */
    private fun playOut(
        board: Board,
        turn: Side,
        perfectSide: Side,
        ai: PerfectAi,
        visited: HashSet<String>,
    ) {
        when (val state = Rules.state(board)) {
            is GameState.Win -> {
                assertEquals("Идеальный соперник проиграл: $board", perfectSide, state.winner)
                return
            }
            GameState.Draw -> return
            is GameState.Playing -> {
                if (!visited.add(board.code() + turn.name + perfectSide.name)) return
                val moves = if (turn == perfectSide) ai.bestMoves(board, perfectSide) else board.emptyCells()
                for (move in moves) playOut(board.withMove(move, turn), turn.opponent, perfectSide, ai, visited)
            }
        }
    }
}
