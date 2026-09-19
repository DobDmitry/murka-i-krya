package murka.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesTest {

    @Test
    fun `восемь линий и ни одной лишней`() {
        assertEquals(8, Rules.LINES.size)
        assertEquals(8, Rules.LINES.map { it.sorted() }.toSet().size)
        assertTrue(Rules.LINES.all { line -> line.size == 3 && line.all { it in 0..8 } })
    }

    @Test
    fun `победа по каждой из восьми линий для обоих персонажей`() {
        for (line in Rules.LINES) {
            for (player in Player.values()) {
                var board = Board.empty()
                val others = (0..8).filterNot { it in line }
                // Линию занимает победитель, соперник ставит фигуры в стороне.
                line.forEachIndexed { index, cell ->
                    board = board.withMove(cell, player)
                    if (index < 2) board = board.withMove(others[index], player.opponent)
                }
                val state = Rules.state(board)
                assertTrue("Линия $line игрока $player не засчитана: $board", state is GameState.Win)
                state as GameState.Win
                assertEquals(player, state.winner)
                assertEquals(line.sorted(), state.line.sorted())
                assertTrue(state.isOver)
            }
        }
    }

    @Test
    fun `ничья на полном поле без линии`() {
        val board = Board.of(
            """
            MMK
            KKM
            MKM
            """
        )
        assertEquals(GameState.Draw, Rules.state(board))
        assertTrue(Rules.state(board).isOver)
        assertTrue(board.isFull)
    }

    @Test
    fun `незаконченная партия — ход того, кто должен ходить`() {
        assertEquals(GameState.Playing(Player.MURKA), Rules.state(Board.empty()))
        val afterFirst = Board.empty().withMove(4, Player.MURKA)
        assertEquals(GameState.Playing(Player.KRYA), Rules.state(afterFirst))
        assertFalse(Rules.state(afterFirst).isOver)
    }

    @Test
    fun `победа важнее полного поля`() {
        val board = Board.of("MMMKKMMKK")
        val state = Rules.state(board)
        assertTrue(state is GameState.Win)
        assertEquals(Player.MURKA, (state as GameState.Win).winner)
    }

    @Test
    fun `выигрышные ходы находятся во всех направлениях`() {
        assertEquals(listOf(2), Rules.winningMoves(Board.of("MM.KK...."), Player.MURKA))
        assertEquals(listOf(5), Rules.winningMoves(Board.of("MM.KK...."), Player.KRYA))
        assertEquals(listOf(8), Rules.winningMoves(Board.of("M...M...."), Player.MURKA))
        assertTrue(Rules.winningMoves(Board.empty(), Player.MURKA).isEmpty())
    }

    @Test
    fun `вилка — это два выигрышных хода сразу`() {
        // Мурка в двух углах, ход в третий угол создаёт две угрозы.
        val board = Board.of("M...K...M")
        val forks = Rules.forkMoves(board, Player.MURKA)
        assertTrue(forks.isNotEmpty())
        for (fork in forks) {
            assertEquals(2, Rules.winningMoves(board.withMove(fork, Player.MURKA), Player.MURKA).size)
        }
    }

    @Test
    fun `соседние клетки считаются по-человечески`() {
        assertEquals(listOf(1, 3, 4), Rules.neighbours(0).sorted())
        assertEquals(listOf(0, 1, 2, 3, 5, 6, 7, 8), Rules.neighbours(4).sorted())
        assertEquals(listOf(0, 1, 4, 6, 7), Rules.neighbours(3).sorted())
    }

    @Test
    fun `поле неизменяемо и разбирается из записи`() {
        val board = Board.empty()
        val next = board.withMove(0, Player.MURKA)
        assertTrue(board.isEmpty(0))
        assertEquals(Player.MURKA, next[0])
        assertEquals("M........", next.code())
        assertEquals(next, Board.of("M........"))
        assertEquals(next.hashCode(), Board.of("M........").hashCode())
    }
}
