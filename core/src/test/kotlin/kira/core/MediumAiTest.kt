package kira.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Средний уровень: видит один ход вперёд, вилок не понимает. */
class MediumAiTest {

    private fun medium(seed: Int = 1) = MediumAi(Random(seed))

    @Test
    fun `забирает свой выигрыш`() {
        val board = Board.of("BB.AA...A")
        assertEquals(2, medium().chooseMove(board, Side.SECOND))
    }

    @Test
    fun `блокирует очевидную угрозу`() {
        val board = Board.of("AA.B..B.A")
        assertEquals(2, medium().chooseMove(board, Side.SECOND))
    }

    @Test
    fun `свой выигрыш важнее блокировки`() {
        val board = Board.of("AA.BB.A..")
        assertEquals(5, medium().chooseMove(board, Side.SECOND))
    }

    @Test
    fun `занимает центр, пока он свободен`() {
        assertEquals(Rules.CENTER, medium().chooseMove(Board.empty(), Side.FIRST))
        assertEquals(Rules.CENTER, medium().chooseMove(Board.of("A........"), Side.SECOND))
    }

    @Test
    fun `без центра берёт угол, а край — в последнюю очередь`() {
        repeat(20) { seed ->
            val corner = medium(seed).chooseMove(Board.of("....A...."), Side.SECOND)
            assertTrue("Ожидали угол, получили $corner", corner in Rules.CORNERS)
        }
        // Свободны только края — тогда край.
        val edge = medium().chooseMove(Board.of("A.A.B.B.A"), Side.SECOND)
        assertTrue("Ожидали край, получили $edge", edge in Rules.EDGES)
    }

    @Test
    fun `вилку не строит и от вилки не защищается`() {
        // Классическая ловушка: первый в двух противоположных углах, второй в центре.
        // Спастись можно только краем, средний же по привычке берёт угол и проигрывает.
        val trap = Board.of("A...B...A")
        repeat(20) { seed ->
            val move = medium(seed).chooseMove(trap, Side.SECOND)
            assertTrue("Средний уровень вдруг поумнел и сходил в $move", move in Rules.CORNERS)
            assertEquals(
                "Угловой ход обязан проигрывать — иначе ловушка не ловушка",
                Outcome.LOSS,
                PerfectPlay.outcome(trap.withMove(move, Side.SECOND), Side.SECOND),
            )
        }
        // А край спасал бы партию.
        for (edge in Rules.EDGES) {
            assertEquals(
                Outcome.DRAW,
                PerfectPlay.outcome(trap.withMove(edge, Side.SECOND), Side.SECOND),
            )
        }
    }

    @Test
    fun `ход всегда легален`() {
        val random = Random(3)
        repeat(300) {
            var board = Board.empty()
            var turn = Rules.FIRST_PLAYER
            while (Rules.state(board) is GameState.Playing) {
                val move =
                    if (turn == Side.SECOND) medium(random.nextInt()).chooseMove(board, turn)
                    else board.emptyCells().random(random)
                assertTrue("Клетка $move занята", board.isEmpty(move))
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
        }
    }
}
