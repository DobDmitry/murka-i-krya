package com.dobdmitry.murkakrya

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.dobdmitry.murkakrya.audio.GameAudio
import com.dobdmitry.murkakrya.ui.Phrase
import com.dobdmitry.murkakrya.ui.Phrases
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import murka.core.Board
import murka.core.Difficulty
import murka.core.GameMode
import murka.core.GameState
import murka.core.Player
import murka.core.Rules
import murka.core.aiFor
import kotlin.random.Random

enum class Screen { START, GAME }

/** Всё, что видно на экране. Партия целиком выводится из [board]. */
data class UiState(
    val screen: Screen = Screen.START,
    val mode: GameMode = GameMode.VERSUS_AI,
    val difficulty: Difficulty = Difficulty.KITTEN,
    val board: Board = Board.empty(),
    val scoreMurka: Int = 0,
    val scoreKrya: Int = 0,
    val soundOn: Boolean = true,
    val roundSeed: Int = 1,
    val erasing: Boolean = false,
    val aiThinking: Boolean = false,
) {
    val state: GameState get() = Rules.state(board)

    val winner: Player? get() = (state as? GameState.Win)?.winner

    val winLine: List<Int>? get() = (state as? GameState.Win)?.line

    val isDraw: Boolean get() = state is GameState.Draw

    /** Надпись, которая сейчас висит над полем. */
    val banner: Phrase
        get() = when (val current = state) {
            is GameState.Win -> Phrases.win(current.winner)
            GameState.Draw -> Phrases.DRAW
            is GameState.Playing -> Phrases.turn(current.turn)
        }
}

/**
 * Состояние игры живёт здесь, поэтому сворачивание приложения и поворот
 * (он заблокирован, но всё же) партию не ломают: счёт и поле сохраняются
 * в SavedStateHandle.
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

    // --- Действия игрока ---------------------------------------------------

    fun chooseTwoPlayers() {
        audio.play(GameAudio.Sfx.BLUP)
        update(ui.copy(mode = GameMode.TWO_PLAYERS, screen = Screen.GAME, board = Board.empty(), roundSeed = ui.roundSeed + 1))
        announceAfter(Phrases.TOGETHER.speech)
    }

    fun chooseAi(difficulty: Difficulty) {
        audio.play(GameAudio.Sfx.BLUP)
        update(
            ui.copy(
                mode = GameMode.VERSUS_AI,
                difficulty = difficulty,
                screen = Screen.GAME,
                board = Board.empty(),
                roundSeed = ui.roundSeed + 1,
            )
        )
        announceAfter(Phrases.level(difficulty).speech)
    }

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
        // Включили — тут же подтверждаем голосом и звуком, чтобы связь была очевидна.
        audio.play(GameAudio.Sfx.BLUP)
        audio.say(Phrases.SOUND.speech, force = true)
    }

    fun resetScore() {
        update(ui.copy(scoreMurka = 0, scoreKrya = 0))
    }

    /** Озвучка надписи — ею пользуется UI при появлении любого слова. */
    fun say(phrase: String) = audio.say(phrase)

    fun blup() = audio.play(GameAudio.Sfx.BLUP)

    // --- Внутреннее --------------------------------------------------------

    private fun makeMove(index: Int, player: Player) {
        audio.play(GameAudio.Sfx.BLUP)
        val board = ui.board.withMove(index, player)
        var next = ui.copy(board = board)
        val state = Rules.state(board)
        if (state is GameState.Win) {
            next = when (state.winner) {
                Player.MURKA -> next.copy(scoreMurka = (next.scoreMurka + 1).coerceAtMost(MAX_SCORE))
                Player.KRYA -> next.copy(scoreKrya = (next.scoreKrya + 1).coerceAtMost(MAX_SCORE))
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
            // Пауза, чтобы ребёнок успел увидеть, что «робот думает».
            delay(600L + random.nextLong(400L))
            val board = ui.board
            val state2 = Rules.state(board)
            if (state2 is GameState.Playing && state2.turn == AI_SIDE) {
                val move = aiFor(ui.difficulty, random).chooseMove(board, AI_SIDE)
                update(ui.copy(aiThinking = false))
                makeMove(move, AI_SIDE)
            } else {
                update(ui.copy(aiThinking = false))
            }
        }
    }

    /** Сначала проговариваем слово кнопки, потом — чей ход: фразы не перебивают друг друга. */
    private fun announceAfter(phrase: String) {
        audio.say(phrase, force = true)
        viewModelScope.launch {
            delay(1000)
            announceBanner(force = true)
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
            state.difficulty.name,
            state.board.code(),
            state.scoreMurka.toString(),
            state.scoreKrya.toString(),
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
                difficulty = Difficulty.valueOf(parts[2]),
                board = Board.of(parts[3]),
                scoreMurka = parts[4].toInt(),
                scoreKrya = parts[5].toInt(),
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
        /** За компьютер всегда играет Кря: ребёнок — Мурка и ходит первым. */
        val AI_SIDE: Player = Player.KRYA

        const val ERASE_MILLIS = 620L
        const val MAX_SCORE = 9
        private const val KEY_STATE = "состояние"
    }
}
