package com.dobdmitry.murkakrya

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dobdmitry.murkakrya.audio.GameAudio
import com.dobdmitry.murkakrya.ui.Cast
import com.dobdmitry.murkakrya.ui.Phrase
import com.dobdmitry.murkakrya.ui.Phrases
import kira.core.Board
import kira.core.Difficulty
import kira.core.GameMode
import kira.core.GameState
import kira.core.Rules
import kira.core.Side
import kira.core.aiFor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class Screen { START, GAME }

/**
 * Всё, что видно на экране. Партия целиком выводится из [board],
 * а кто именно играет — из [opponent]: Кира ходит первой всегда.
 */
data class UiState(
    val screen: Screen = Screen.START,
    val mode: GameMode = GameMode.VERSUS_AI,
    val opponent: Cast = Cast.DEFAULT_OPPONENT,
    val board: Board = Board.empty(),
    val scoreHero: Int = 0,
    val scoreOpponent: Int = 0,
    val soundOn: Boolean = true,
    val roundSeed: Int = 1,
    val erasing: Boolean = false,
    val aiThinking: Boolean = false,
) {
    val hero: Cast get() = Cast.HERO

    fun castOf(side: Side): Cast = if (side == Side.FIRST) hero else opponent

    val state: GameState get() = Rules.state(board)

    val winnerSide: Side? get() = (state as? GameState.Win)?.winner

    val winner: Cast? get() = winnerSide?.let { castOf(it) }

    val winLine: List<Int>? get() = (state as? GameState.Win)?.line

    val isDraw: Boolean get() = state is GameState.Draw

    val turn: Side? get() = (state as? GameState.Playing)?.turn

    val difficulty: Difficulty get() = opponent.difficulty ?: Difficulty.EASY

    /** Надпись, которая сейчас висит над полем. */
    val banner: Phrase
        get() = when (val current = state) {
            is GameState.Win -> Phrases.win(castOf(current.winner))
            GameState.Draw -> Phrases.DRAW
            is GameState.Playing -> Phrases.turn(castOf(current.turn))
        }
}

/**
 * Состояние игры живёт здесь, поэтому сворачивание приложения партию не ломает:
 * поле, счёт и выбранный соперник сохраняются в SavedStateHandle.
 */
class GameViewModel(
    application: Application,
    private val handle: SavedStateHandle,
) : AndroidViewModel(application) {

    private val audio = GameAudio(application)
    private val random = Random(System.currentTimeMillis())
    private var aiJob: Job? = null

    var ui by mutableStateOf(restore())
        private set

    init {
        audio.enabled = ui.soundOn
        scheduleAiMoveIfNeeded()
    }

    // --- Выбор соперника ---------------------------------------------------

    /** Игра один на один: Кира против мамы или папы. */
    fun playWithPerson(person: Cast) = startGame(GameMode.TWO_PLAYERS, person)

    /** Игра против компьютера: соперник — зверь, его сила зашита в него самого. */
    fun playWithAnimal(animal: Cast) = startGame(GameMode.VERSUS_AI, animal)

    private fun startGame(mode: GameMode, opponent: Cast) {
        aiJob?.cancel()
        audio.play(GameAudio.Sfx.BLUP)
        update(
            ui.copy(
                screen = Screen.GAME,
                mode = mode,
                opponent = opponent,
                board = Board.empty(),
                scoreHero = 0,
                scoreOpponent = 0,
                erasing = false,
                aiThinking = false,
                roundSeed = ui.roundSeed + 1,
            )
        )
        // Сначала имя соперника, через секунду — чей ход: фразы не перебивают друг друга.
        audio.say(opponent.speech, force = true)
        viewModelScope.launch {
            delay(1000)
            announceBanner(force = true)
        }
    }

    // --- Действия в партии -------------------------------------------------

    fun tapCell(index: Int) {
        val current = ui
        if (current.screen != Screen.GAME) return
        if (current.erasing || current.aiThinking) return
        val state = current.state
        if (state !is GameState.Playing) return
        if (!current.board.isEmpty(index)) return
        if (current.mode == GameMode.VERSUS_AI && state.turn == AI_SIDE) return
        makeMove(index, state.turn)
    }

    fun newRound() {
        aiJob?.cancel()
        audio.play(GameAudio.Sfx.ERASE)
        audio.say(Phrases.AGAIN.speech, force = true)
        update(ui.copy(erasing = true, aiThinking = false))
        viewModelScope.launch {
            delay(ERASE_MILLIS)
            update(ui.copy(board = Board.empty(), erasing = false, roundSeed = ui.roundSeed + 1))
            announceBanner(force = true)
            scheduleAiMoveIfNeeded()
        }
    }

    fun goHome() {
        aiJob?.cancel()
        audio.play(GameAudio.Sfx.BLUP)
        audio.forgetLastPhrase()
        update(ui.copy(screen = Screen.START, board = Board.empty(), erasing = false, aiThinking = false))
    }

    fun toggleSound() {
        val on = !ui.soundOn
        audio.enabled = on
        update(ui.copy(soundOn = on))
        audio.play(GameAudio.Sfx.BLUP)
        audio.say(Phrases.SOUND.speech, force = true)
    }

    /** Озвучка надписи — ею пользуется интерфейс при появлении любого слова. */
    fun say(phrase: String) = audio.say(phrase)

    fun blup() = audio.play(GameAudio.Sfx.BLUP)

    // --- Внутреннее --------------------------------------------------------

    private fun makeMove(index: Int, side: Side) {
        audio.play(GameAudio.Sfx.BLUP)
        val board = ui.board.withMove(index, side)
        var next = ui.copy(board = board)
        val state = Rules.state(board)
        if (state is GameState.Win) {
            next = when (state.winner) {
                Side.FIRST -> next.copy(scoreHero = (next.scoreHero + 1).coerceAtMost(MAX_SCORE))
                Side.SECOND -> next.copy(scoreOpponent = (next.scoreOpponent + 1).coerceAtMost(MAX_SCORE))
            }
        }
        update(next)

        viewModelScope.launch {
            delay(120)
            audio.play(GameAudio.Sfx.PLACE)
            when (state) {
                is GameState.Win -> {
                    delay(420)
                    audio.play(GameAudio.Sfx.WIN)
                    delay(260)
                    audio.play(GameAudio.Sfx.STAR)
                }
                GameState.Draw -> {
                    delay(380)
                    audio.play(GameAudio.Sfx.DRAW)
                }
                is GameState.Playing -> Unit
            }
            announceBanner()
        }

        scheduleAiMoveIfNeeded()
    }

    private fun scheduleAiMoveIfNeeded() {
        val current = ui
        if (current.screen != Screen.GAME || current.mode != GameMode.VERSUS_AI) return
        val state = current.state
        if (state !is GameState.Playing || state.turn != AI_SIDE) return

        aiJob?.cancel()
        update(current.copy(aiThinking = true))
        aiJob = viewModelScope.launch {
            // Пауза, чтобы ребёнок успел увидеть, что соперник думает.
            delay(600L + random.nextLong(400L))
            val board = ui.board
            val now = Rules.state(board)
            if (now is GameState.Playing && now.turn == AI_SIDE) {
                val move = aiFor(ui.difficulty, random).chooseMove(board, AI_SIDE)
                update(ui.copy(aiThinking = false))
                makeMove(move, AI_SIDE)
            } else {
                update(ui.copy(aiThinking = false))
            }
        }
    }

    private fun announceBanner(force: Boolean = false) {
        audio.say(ui.banner.speech, force = force)
    }

    private fun update(next: UiState) {
        ui = next
        persist(next)
    }

    private fun persist(state: UiState) {
        handle[KEY_STATE] = listOf(
            state.screen.name,
            state.mode.name,
            state.opponent.name,
            state.board.code(),
            state.scoreHero.toString(),
            state.scoreOpponent.toString(),
            if (state.soundOn) "1" else "0",
            state.roundSeed.toString(),
        ).joinToString("|")
    }

    private fun restore(): UiState {
        val saved = handle.get<String>(KEY_STATE) ?: return UiState()
        return runCatching {
            val parts = saved.split("|")
            UiState(
                screen = Screen.valueOf(parts[0]),
                mode = GameMode.valueOf(parts[1]),
                opponent = Cast.valueOf(parts[2]),
                board = Board.of(parts[3]),
                scoreHero = parts[4].toInt(),
                scoreOpponent = parts[5].toInt(),
                soundOn = parts[6] == "1",
                roundSeed = parts[7].toInt(),
            )
        }.getOrElse { UiState() }
    }

    override fun onCleared() {
        aiJob?.cancel()
        audio.release()
        super.onCleared()
    }

    companion object {
        /** Кира ходит первой, компьютер всегда играет вторым. */
        val AI_SIDE: Side = Side.SECOND

        const val ERASE_MILLIS = 620L
        const val MAX_SCORE = 9
        private const val KEY_STATE = "состояние"
    }
}
