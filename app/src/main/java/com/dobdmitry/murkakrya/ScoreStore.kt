package com.dobdmitry.murkakrya

import android.content.Context
import com.dobdmitry.murkakrya.ui.Cast

/**
 * Счёт живёт между запусками и отдельно на каждого соперника:
 * «с папой 3:2» и «с волком 0:5» — это разные истории.
 *
 * Хранится в приватных настройках приложения, никуда не отправляется.
 */
class ScoreStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("счёт", Context.MODE_PRIVATE)

    fun heroWins(opponent: Cast): Int = prefs.getInt(heroKey(opponent), 0)

    fun opponentWins(opponent: Cast): Int = prefs.getInt(opponentKey(opponent), 0)

    fun addHeroWin(opponent: Cast): Int {
        val next = heroWins(opponent) + 1
        prefs.edit().putInt(heroKey(opponent), next).apply()
        return next
    }

    fun addOpponentWin(opponent: Cast): Int {
        val next = opponentWins(opponent) + 1
        prefs.edit().putInt(opponentKey(opponent), next).apply()
        return next
    }

    /** Победы Киры подряд — по всем соперникам сразу. */
    var streak: Int
        get() = prefs.getInt(KEY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    fun clear(opponent: Cast) {
        prefs.edit()
            .putInt(heroKey(opponent), 0)
            .putInt(opponentKey(opponent), 0)
            .apply()
    }

    private fun heroKey(opponent: Cast) = "кира_против_${opponent.name}"

    private fun opponentKey(opponent: Cast) = "${opponent.name}_против_киры"

    private companion object {
        const val KEY_STREAK = "подряд"
    }
}
