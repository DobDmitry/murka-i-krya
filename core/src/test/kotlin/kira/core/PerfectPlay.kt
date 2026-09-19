package kira.core

/** Чем закончится позиция, если дальше обе стороны играют идеально. */
enum class Outcome { WIN, DRAW, LOSS }

object PerfectPlay {

    private val cache = HashMap<String, Outcome>()

    fun outcome(board: Board, forPlayer: Side): Outcome {
        val key = board.code() + forPlayer.name
        cache[key]?.let { return it }
        val result = when (val state = Rules.state(board)) {
            is GameState.Win -> if (state.winner == forPlayer) Outcome.WIN else Outcome.LOSS
            GameState.Draw -> Outcome.DRAW
            is GameState.Playing -> {
                val results = board.emptyCells().map {
                    outcome(board.withMove(it, state.turn), forPlayer)
                }
                if (state.turn == forPlayer) {
                    when {
                        Outcome.WIN in results -> Outcome.WIN
                        Outcome.DRAW in results -> Outcome.DRAW
                        else -> Outcome.LOSS
                    }
                } else {
                    when {
                        Outcome.LOSS in results -> Outcome.LOSS
                        Outcome.DRAW in results -> Outcome.DRAW
                        else -> Outcome.WIN
                    }
                }
            }
        }
        cache[key] = result
        return result
    }
}
