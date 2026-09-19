package com.dobdmitry.murkakrya.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import com.dobdmitry.murkakrya.R
import java.util.Locale

/**
 * Весь звук игры: короткие сигналы и голос.
 *
 * Голос — системный синтезатор Android (офлайн, если на телефоне стоит русский
 * речевой пакет). Нет голоса — игра просто молчит, но звуки и картинка остаются:
 * играть можно и не читая, и не слушая.
 */
class GameAudio(context: Context) {

    enum class Sfx { BLUP, PLACE, WIN, DRAW, STAR, ERASE }

    private val appContext = context.applicationContext
    private val ids = mutableMapOf<Sfx, Int>()
    private val loaded = mutableSetOf<Int>()

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var speech: TextToSpeech? = null
    private var speechReady = false
    private var lastPhrase: String? = null

    /** Общий выключатель: кнопка «ЗВУК» в углу. */
    var enabled: Boolean = true

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded += sampleId
        }
        ids[Sfx.BLUP] = pool.load(appContext, R.raw.blup, 1)
        ids[Sfx.PLACE] = pool.load(appContext, R.raw.place, 1)
        ids[Sfx.WIN] = pool.load(appContext, R.raw.win, 1)
        ids[Sfx.DRAW] = pool.load(appContext, R.raw.draw, 1)
        ids[Sfx.STAR] = pool.load(appContext, R.raw.star, 1)
        ids[Sfx.ERASE] = pool.load(appContext, R.raw.erase, 1)

        speech = TextToSpeech(appContext) { status ->
            val engine = speech
            if (status == TextToSpeech.SUCCESS && engine != null) {
                val result = engine.setLanguage(Locale("ru", "RU"))
                speechReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
                if (speechReady) {
                    engine.setSpeechRate(0.92f)
                    engine.setPitch(1.18f)
                }
            }
        }
    }

    fun play(sfx: Sfx, volume: Float = 1f, rate: Float = 1f) {
        if (!enabled) return
        val id = ids[sfx] ?: return
        if (id !in loaded) return
        pool.play(id, volume, volume, 1, 0, rate)
    }

    /** Проговаривает надпись. Повтор той же фразы подряд игнорируется. */
    fun say(phrase: String, force: Boolean = false) {
        if (!enabled) return
        if (!speechReady) return
        if (!force && phrase == lastPhrase) return
        lastPhrase = phrase
        speech?.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, phrase)
    }

    fun forgetLastPhrase() {
        lastPhrase = null
    }

    fun release() {
        pool.release()
        speech?.stop()
        speech?.shutdown()
        speech = null
    }
}
