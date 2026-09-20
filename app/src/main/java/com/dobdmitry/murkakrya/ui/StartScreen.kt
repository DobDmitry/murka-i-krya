package com.dobdmitry.murkakrya.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Стартовый экран: ни меню, ни настроек, только лица.
 *
 * Сверху Кира — она играет всегда. Ниже два живых соперника (мама и папа):
 * это игра вдвоём на одном телефоне. Ещё ниже девять знакомых лиц —
 * это игра с компьютером, и все они играют одинаково легко.
 */
@Composable
fun StartScreen(
    soundOn: Boolean,
    onPerson: (Cast) -> Unit,
    onComputer: (Cast) -> Unit,
    onToggleSound: () -> Unit,
    onExit: () -> Unit,
) {
    val speaker = LocalSpeaker.current
    LaunchedEffect(Unit) { speaker("Кира, с кем играем?") }

    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) { drawWarmBackground() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(soundOn = soundOn, onToggleSound = onToggleSound, onExit = onExit)

            // Хозяйка игры
            LivingCharacter(
                character = Cast.HERO,
                modifier = Modifier.size(128.dp),
                seed = 0,
            )
            SpokenLabel(
                phrase = Phrases.name(Cast.HERO),
                style = MaterialTheme.typography.displayMedium,
                speak = false,
            )

            Spacer(Modifier.height(14.dp))

            // Живые соперники: играем вдвоём на одном телефоне
            PeopleBadge(Modifier.size(58.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (person in Cast.PEOPLE) {
                    CharacterButton(
                        character = person,
                        onClick = { onPerson(person) },
                        iconSize = 104.dp,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Соперники за компьютер: девять лиц, все играют одинаково легко
            RobotBadge(Modifier.size(58.dp))
            val rivals = Cast.COMPUTER
            for (rowIndex in 0 until (rivals.size + 2) / 3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (rival in rivals.drop(rowIndex * 3).take(3)) {
                        CharacterButton(
                            character = rival,
                            onClick = { onComputer(rival) },
                            iconSize = 82.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
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
            Spacer(Modifier.width(64.dp))
        }

        BigButton(
            phrase = Phrases.SOUND,
            onClick = onToggleSound,
            iconSize = 46.dp,
            icon = { modifier ->
                IconCanvas(modifier) { center, radius ->
                    drawSpeaker(center, radius, soundOn, Palette.Ink)
                }
            },
        )
    }
}
