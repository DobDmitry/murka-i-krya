package com.dobdmitry.murkakrya

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dobdmitry.murkakrya.ui.GameScreen
import com.dobdmitry.murkakrya.ui.LocalCastImages
import com.dobdmitry.murkakrya.ui.LocalSpeaker
import com.dobdmitry.murkakrya.ui.rememberCastImages
import com.dobdmitry.murkakrya.ui.GameTheme
import com.dobdmitry.murkakrya.ui.StartScreen

/**
 * Единственный экран приложения. Ориентация portrait и запрет случайного выхода
 * заданы в манифесте и здесь: ребёнок не должен вылететь из игры кнопкой «назад».
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Пока играют, экран не гаснет: партия у четырёхлетки идёт не быстро.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            GameTheme {
                val model: GameViewModel = viewModel()
                val ui = model.ui

                // Кнопка «назад» игру не закрывает. Выход — только долгим нажатием на домик.
                BackHandler(enabled = true) { model.blup() }

                CompositionLocalProvider(
                    LocalSpeaker provides { phrase -> model.say(phrase) },
                    LocalCastImages provides rememberCastImages(),
                ) {
                    when (ui.screen) {
                        Screen.START -> StartScreen(
                            soundOn = ui.soundOn,
                            onPerson = model::playWithPerson,
                            onAnimal = model::playWithAnimal,
                            onToggleSound = model::toggleSound,
                            onExit = { finish() },
                        )

                        Screen.GAME -> GameScreen(
                            ui = ui,
                            onCellTap = model::tapCell,
                            onAgain = model::newRound,
                            onHome = model::goHome,
                            onToggleSound = model::toggleSound,
                            onResetScore = model::resetScore,
                        )
                    }
                }
            }
        }
    }
}
