package murka.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** ЛЕВ — уровень «ТРУДНО». Проверяем, что минимакс действительно идеален. */
class LionAiTest {

    private fun lion(seed: Int = 1) = LionAi(Random(seed))

    @Test
    fun `забирает выигрыш немедленно`() {
        // Кря стоит на 0 и 1, Мурка грозит из 3-4. Выигрыш важнее защиты.
        val board = Board.of("KK.MM...M")
        assertEquals(listOf(2), lion().bestMoves(board, Player.KRYA))
    }

    @Test
    fun `блокирует чужой выигрыш, когда своего нет`() {
        // Мурка собрала 0-1, спасает только клетка 2.
        val board = Board.of("MM..K....")
        assertEquals(listOf(2), lion().bestMoves(board, Player.KRYA))
    }

    @Test
    fun `свой выигрыш важнее чужой угрозы`() {
        val board = Board.of("MM.KK.M..")
        assertEquals(listOf(5), lion().bestMoves(board, Player.KRYA))
    }

    @Test
    fun `наказывает ошибку соперника и доводит до победы`() {
        // Мурка пошла в угол, Кря ответила углом вместо центра — это проигрыш.
        val mistake = Board.of("M.K......")
        val best = lion().bestMoves(mistake, Player.MURKA)
        assertTrue("Лев обязан найти выигрывающий план", best.isNotEmpty())
        for (move in best) {
            val after = mistake.withMove(move, Player.MURKA)
            assertEquals(
                "Ход $move не доводит до победы: $after",
                Outcome.WIN,
                PerfectPlay.outcome(after, forPlayer = Player.MURKA),
            )
        }
    }

    @Test
    fun `лев никогда не проигрывает — полный перебор за оба цвета`() {
        for (lionSide in Player.values()) {
            playOut(Board.empty(), Rules.FIRST_PLAYER, lionSide, lion(), HashSet())
        }
    }

    @Test
    fun `лев против льва — всегда ничья`() {
        val ai = lion()
        val visited = HashSet<String>()
        fun walk(board: Board, turn: Player) {
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
    fun `случайного соперника лев наказывает почти всегда`() {
        val random = Random(7)
        val ai = LionAi(random)
        val lionSide = Player.KRYA
        var lionWins = 0
        var draws = 0
        repeat(200) {
            var board = Board.empty()
            var turn = Rules.FIRST_PLAYER
            while (Rules.state(board) is GameState.Playing) {
                val move =
                    if (turn == lionSide) ai.chooseMove(board, lionSide)
                    else board.emptyCells().random(random)
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
            when (val state = Rules.state(board)) {
                is GameState.Win -> {
                    assertEquals("Лев проиграл случайному сопернику: $board", lionSide, state.winner)
                    lionWins++
                }
                else -> draws++
            }
        }
        assertEquals(200, lionWins + draws)
        assertTrue("Лев выиграл лишь $lionWins из 200", lionWins > 120)
    }

    /** Перебираем все ответы соперника и все равноценные ходы льва. */
    private fun playOut(
        board: Board,
        turn: Player,
        lionSide: Player,
        ai: LionAi,
        visited: HashSet<String>,
    ) {
        when (val state = Rules.state(board)) {
            is GameState.Win -> {
                assertEquals("Лев проиграл: $board", lionSide, state.winner)
                return
            }
            GameState.Draw -> return
            is GameState.Playing -> {
                if (!visited.add(board.code() + turn.name + lionSide.name)) return
                val moves = if (turn == lionSide) ai.bestMoves(board, lionSide) else board.emptyCells()
                for (move in moves) playOut(board.withMove(move, turn), turn.opponent, lionSide, ai, visited)
            }
        }
    }
}
