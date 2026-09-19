package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dobdmitry.murkakrya.UiState
import murka.core.GameMode
import murka.core.GameState
import murka.core.Player

/**
 * Игровой экран: счёт, чей ход, поле во весь экран и одна большая кнопка «ЕЩЁ РАЗ».
 * Текста ровно столько, сколько ребёнок успевает прочесть.
 */
@Composable
fun GameScreen(
    ui: UiState,
    onCellTap: (Int) -> Unit,
    onAgain: () -> Unit,
    onHome: () -> Unit,
    onToggleSound: () -> Unit,
) {
    val eraseProgress by animateFloatAsState(
        targetValue = if (ui.erasing) 1f else 0f,
        animationSpec = tween(durationMillis = if (ui.erasing) 620 else 0),
        label = "ластик",
    )

    val playing = ui.state as? GameState.Playing
    val humanTurn = playing != null &&
        (ui.mode == GameMode.TWO_PLAYERS || playing.turn == Player.MURKA)
    val boardEnabled = humanTurn && !ui.erasing && !ui.aiThinking

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { drawWarmBackground() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(soundOn = ui.soundOn, onToggleSound = onToggleSound, onHome = onHome)

            ScoreBoard(ui)

            Banner(ui)

            BoardView(
                board = ui.board,
                winLine = ui.winLine,
                winner = ui.winner,
                eraseProgress = eraseProgress,
                enabled = boardEnabled,
                roundSeed = ui.roundSeed,
                onCellTap = onCellTap,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )

            Spacer(Modifier.weight(1f))

            BigButton(
                phrase = Phrases.AGAIN,
                onClick = onAgain,
                iconSize = 58.dp,
                highlighted = ui.state.isOver,
            ) { modifier ->
                IconCanvas(modifier) { center, radius -> drawRepeat(center, radius, Palette.Mint) }
            }

            Spacer(Modifier.height(6.dp))
        }

        ConfettiOverlay(
            playing = ui.winner != null,
            seed = ui.roundSeed,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Ряд звёздочек и крупная цифра — счёт понятен и ребёнку, и папе. */
@Composable
private fun ScoreBoard(ui: UiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScorePanel(Player.MURKA, ui.scoreMurka)
        ScorePanel(Player.KRYA, ui.scoreKrya)
    }
}

@Composable
private fun ScorePanel(player: Player, score: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CharacterIcon(player, Modifier.size(42.dp))
        Spacer(Modifier.width(4.dp))
        StarRow(
            count = score,
            color = Palette.of(player),
            modifier = Modifier
                .width(92.dp)
                .height(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = Palette.darkOf(player),
        )
    }
}

/**
 * Чей ход. Активный персонаж крупный и пульсирует, ждущий — притушен.
 * Ничья показывается обнимашкой: это не неудача, а дружба.
 */
@Composable
private fun Banner(ui: UiState) {
    val draw = ui.isDraw
    val winner = ui.winner
    val turn = (ui.state as? GameState.Playing)?.turn

    val murkaActive = winner == Player.MURKA || turn == Player.MURKA || draw
    val kryaActive = winner == Player.KRYA || turn == Player.KRYA || draw

    val murkaPulse = rememberPulse(active = turn == Player.MURKA || winner == Player.MURKA)
    val kryaPulse = rememberPulse(active = turn == Player.KRYA || winner == Player.KRYA)

    val hug by animateDpAsState(
        targetValue = if (draw) 18.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "обнимашка",
    )
    val tilt by animateFloatAsState(
        targetValue = if (draw) 16f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "наклон",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (draw) {
            Canvas(Modifier.fillMaxSize()) {
                drawHearts(progress = 1f)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LivingCharacter(
                player = Player.MURKA,
                dimmed = !murkaActive,
                extraScale = 1f + murkaPulse * 0.10f,
                rotationDegrees = tilt,
                seed = 0,
                modifier = Modifier
                    .size(if (winner == Player.MURKA) 92.dp else 76.dp)
                    .offset(x = hug),
            )
            SpokenLabel(
                phrase = ui.banner,
                style = if (winner != null) MaterialTheme.typography.headlineLarge
                else MaterialTheme.typography.headlineMedium,
                color = when {
                    winner != null -> Palette.darkOf(winner)
                    draw -> Palette.Ink
                    else -> Palette.Ink
                },
                speak = false,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )
            LivingCharacter(
                player = Player.KRYA,
                dimmed = !kryaActive,
                extraScale = 1f + kryaPulse * 0.10f,
                rotationDegrees = -tilt,
                seed = 2,
                modifier = Modifier
                    .size(if (winner == Player.KRYA) 92.dp else 76.dp)
                    .offset(x = -hug),
            )
        }
    }
}
