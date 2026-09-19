package com.dobdmitry.murkakrya.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import murka.core.Difficulty
import murka.core.Player

/**
 * Стартовый экран: никаких меню и инструкций, только иконки.
 * Две мордочки — играем вдвоём, мордочка с роботом — играем с компьютером.
 */
@Composable
fun StartScreen(
    soundOn: Boolean,
    difficulty: Difficulty,
    onTwoPlayers: () -> Unit,
    onAi: (Difficulty) -> Unit,
    onToggleSound: () -> Unit,
    onExit: () -> Unit,
) {
    var levelsVisible by remember { mutableStateOf(false) }
    val speaker = LocalSpeaker.current
    androidx.compose.runtime.LaunchedEffect(Unit) { speaker("Мурка и Кря. Играем!") }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { drawWarmBackground() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(soundOn = soundOn, onToggleSound = onToggleSound, onExit = onExit)

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NameCard(Player.MURKA)
                NameCard(Player.KRYA)
            }

            Spacer(Modifier.height(24.dp))

            BigButton(
                phrase = Phrases.TOGETHER,
                onClick = onTwoPlayers,
                iconSize = 104.dp,
            ) { modifier -> TwoFacesIcon(modifier) }

            Spacer(Modifier.height(18.dp))

            BigButton(
                phrase = Phrases.ROBOT,
                onClick = { levelsVisible = !levelsVisible },
                iconSize = 104.dp,
                highlighted = levelsVisible,
            ) { modifier -> FaceAndRobotIcon(modifier) }

            AnimatedVisibility(
                visible = levelsVisible,
                enter = fadeIn() + expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (level in Difficulty.values()) {
                        BigButton(
                            phrase = Phrases.level(level),
                            onClick = { onAi(level) },
                            iconSize = 72.dp,
                            highlighted = level == difficulty,
                        ) { modifier -> LevelIcon(level, modifier) }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun NameCard(player: Player) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LivingCharacter(
            player = player,
            modifier = Modifier.size(116.dp),
            seed = if (player == Player.MURKA) 0 else 2,
        )
        SpokenLabel(
            phrase = Phrases.name(player),
            style = MaterialTheme.typography.headlineMedium,
            speak = false,
        )
    }
}

@Composable
fun TopBar(
    soundOn: Boolean,
    onToggleSound: () -> Unit,
    onExit: (() -> Unit)? = null,
    onHome: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val holdAction = onHome ?: onExit
        if (holdAction != null) {
            HoldButton(
                onHold = holdAction,
                modifier = Modifier.size(64.dp),
            ) { modifier ->
                IconCanvas(modifier) { center, radius -> drawHome(center, radius, Palette.Pencil) }
            }
        } else {
            Spacer(Modifier.size(64.dp))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BigButton(
                phrase = Phrases.SOUND,
                onClick = onToggleSound,
                iconSize = 46.dp,
                background = Palette.Cream,
                icon = { modifier ->
                    IconCanvas(modifier) { center, radius ->
                        drawSpeaker(center, radius, soundOn, Palette.Ink)
                    }
                },
            )
        }
    }
}
