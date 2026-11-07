package com.example.imoprep

import android.content.Context

data class Stat(val attempts: Int, val best: Int, val total: Int, val last: Int)

/** Saves quiz results on the phone with SharedPreferences. */
object ProgressStore {
    private const val PREFS = "imoprep_progress"

    fun record(context: Context, key: String, score: Int, total: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val attempts = prefs.getInt("$key.attempts", 0) + 1
        val best = maxOf(prefs.getInt("$key.best", -1), score)
        prefs.edit()
            .putInt("$key.attempts", attempts)
            .putInt("$key.best", best)
            .putInt("$key.total", total)
            .putInt("$key.last", score)
            .apply()
    }

    fun read(context: Context, key: String): Stat? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val attempts = prefs.getInt("$key.attempts", 0)
        if (attempts == 0) return null
        return Stat(
            attempts = attempts,
            best = prefs.getInt("$key.best", 0),
            total = prefs.getInt("$key.total", 0),
            last = prefs.getInt("$key.last", 0),
        )
    }
}
