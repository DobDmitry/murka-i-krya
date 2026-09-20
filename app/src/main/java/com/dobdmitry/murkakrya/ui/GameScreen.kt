package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dobdmitry.murkakrya.UiState
import kira.core.GameMode
import kira.core.GameState
import kira.core.Side

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
    onResetScore: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    // Победа отзывается в руке — ребёнку это нравится не меньше конфетти.
    LaunchedEffect(ui.winner, ui.roundSeed) {
        if (ui.winner != null) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    val eraseProgress by animateFloatAsState(
        targetValue = if (ui.erasing) 1f else 0f,
        animationSpec = tween(durationMillis = if (ui.erasing) 620 else 0),
        label = "ластик",
    )

    val playing = ui.state as? GameState.Playing
    val humanTurn = playing != null &&
        (ui.mode == GameMode.TWO_PLAYERS || playing.turn == Side.FIRST)
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

            HoldRow(onHold = onResetScore, modifier = Modifier.fillMaxWidth()) {
                ScoreBoard(ui)
            }

            Banner(ui)

            BoardView(
                board = ui.board,
                heroCast = ui.hero,
                opponentCast = ui.opponent,
                winLine = ui.winLine,
                winner = ui.winner,
                eraseProgress = eraseProgress,
                enabled = boardEnabled,
                roundSeed = ui.roundSeed,
                onCellTap = { index ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCellTap(index)
                },
                // fill = false: на невысоком экране поле уменьшится, но не обрежется.
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )

            Spacer(Modifier.height(10.dp))

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
            count = if (ui.celebrate) 150 else 70,
            modifier = Modifier.fillMaxSize(),
        )

        if (ui.celebrate) {
            ChampionOverlay(modifier = Modifier.fillMaxSize())
        }
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
        ScorePanel(ui.hero, ui.scoreHero)
        ScorePanel(ui.opponent, ui.scoreOpponent)
    }
}

@Composable
private fun ScorePanel(character: Cast, score: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CharacterIcon(character, Modifier.size(42.dp))
        Spacer(Modifier.width(4.dp))
        StarRow(
            count = score,
            color = Palette.of(character),
            modifier = Modifier
                .width(92.dp)
                .height(20.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = Palette.darkOf(character),
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
    val winnerSide = ui.winnerSide
    val turn = ui.turn

    val heroActive = winnerSide == Side.FIRST || turn == Side.FIRST || draw
    val opponentActive = winnerSide == Side.SECOND || turn == Side.SECOND || draw

    val heroPulse = rememberPulse(active = turn == Side.FIRST || winnerSide == Side.FIRST)
    val opponentPulse = rememberPulse(active = turn == Side.SECOND || winnerSide == Side.SECOND)

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
            .height(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (draw) {
            Canvas(Modifier.fillMaxSize()) { drawHearts(progress = 1f) }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            LivingCharacter(
                character = ui.hero,
                dimmed = !heroActive,
                extraScale = 1f + heroPulse * 0.10f,
                rotationDegrees = tilt,
                mood = moodOf(Side.FIRST, winnerSide, turn, draw),
                seed = 0,
                modifier = Modifier
                    .size(if (winnerSide == Side.FIRST) 92.dp else 76.dp)
                    .offset(x = hug),
            )
            SpokenLabel(
                phrase = ui.banner,
                style = if (winnerSide != null) MaterialTheme.typography.headlineLarge
                else MaterialTheme.typography.headlineMedium,
                color = ui.winner?.darkColor ?: Palette.Ink,
                speak = false,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )
            LivingCharacter(
                character = ui.opponent,
                dimmed = !opponentActive,
                extraScale = 1f + opponentPulse * 0.10f,
                rotationDegrees = -tilt,
                mood = moodOf(Side.SECOND, winnerSide, turn, draw),
                seed = 2,
                modifier = Modifier
                    .size(if (winnerSide == Side.SECOND) 92.dp else 76.dp)
                    .offset(x = -hug),
            )
        }
    }
}

/** Настроение персонажа по ходу партии. */
private fun moodOf(side: Side, winner: Side?, turn: Side?, draw: Boolean): Mood = when {
    draw -> Mood.IDLE
    winner == side -> Mood.WINNER
    winner != null -> Mood.LOSER
    turn == side -> Mood.ACTIVE
    else -> Mood.IDLE
}

/** Три победы подряд: кубок вылетает на весь экран. */
@Composable
private fun ChampionOverlay(modifier: Modifier = Modifier) {
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pop.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow),
        )
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
                rotationZ = (1f - pop.value) * 25f
            },
        ) {
            TrophyIcon(Modifier.size(160.dp))
            SpokenLabel(
                phrase = Phrases.CHAMPION,
                style = MaterialTheme.typography.displayMedium,
                color = Palette.KryaDark,
                speak = false,
            )
        }
    }
}
