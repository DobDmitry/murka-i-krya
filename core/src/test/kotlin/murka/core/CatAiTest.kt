package murka.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** КОТ — уровень «СРЕДНЕ»: видит один ход вперёд, вилок не понимает. */
class CatAiTest {

    private fun cat(seed: Int = 1) = CatAi(Random(seed))

    @Test
    fun `забирает свой выигрыш`() {
        val board = Board.of("KK.MM...M")
        assertEquals(2, cat().chooseMove(board, Player.KRYA))
    }

    @Test
    fun `блокирует очевидную угрозу`() {
        val board = Board.of("MM.K..K.M")
        assertEquals(2, cat().chooseMove(board, Player.KRYA))
    }

    @Test
    fun `свой выигрыш важнее блокировки`() {
        val board = Board.of("MM.KK.M..")
        assertEquals(5, cat().chooseMove(board, Player.KRYA))
    }

    @Test
    fun `занимает центр, пока он свободен`() {
        assertEquals(Rules.CENTER, cat().chooseMove(Board.empty(), Player.MURKA))
        assertEquals(Rules.CENTER, cat().chooseMove(Board.of("M........"), Player.KRYA))
    }

    @Test
    fun `без центра берёт угол, а край — в последнюю очередь`() {
        repeat(20) { seed ->
            val corner = cat(seed).chooseMove(Board.of("....M...."), Player.KRYA)
            assertTrue("Ожидали угол, получили $corner", corner in Rules.CORNERS)
        }
        // Свободны только края — тогда край.
        val edge = cat().chooseMove(Board.of("M.M.K.K.M"), Player.KRYA)
        assertTrue("Ожидали край, получили $edge", edge in Rules.EDGES)
    }

    @Test
    fun `вилку не строит и от вилки не защищается`() {
        // Классическая ловушка: Мурка в двух противоположных углах, Кря в центре.
        // Спастись можно только краем, кот же по привычке берёт угол и проигрывает.
        val trap = Board.of("M...K...M")
        repeat(20) { seed ->
            val move = cat(seed).chooseMove(trap, Player.KRYA)
            assertTrue("Кот вдруг поумнел и сходил в $move", move in Rules.CORNERS)
            assertEquals(
                "Угловой ход обязан проигрывать — иначе ловушка не ловушка",
                Outcome.LOSS,
                PerfectPlay.outcome(trap.withMove(move, Player.KRYA), Player.KRYA),
            )
        }
        // А край спасал бы партию.
        for (edge in Rules.EDGES) {
            assertEquals(
                Outcome.DRAW,
                PerfectPlay.outcome(trap.withMove(edge, Player.KRYA), Player.KRYA),
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
                    if (turn == Player.KRYA) cat(random.nextInt()).chooseMove(board, turn)
                    else board.emptyCells().random(random)
                assertTrue("Клетка $move занята", board.isEmpty(move))
                board = board.withMove(move, turn)
                turn = turn.opponent
            }
        }
    }
}
