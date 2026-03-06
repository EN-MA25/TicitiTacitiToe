package com.example.ticititacititoe.util

import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.pow

object Util {

    fun formatTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    fun formatDate(timestamp: Timestamp, format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    fun newRating(myRating: Int, opponentRating: Int, gamesPlayed: Int, score: Double): Int {
        val kValue = when {
            gamesPlayed <= 10 -> 60
            gamesPlayed <= 30 -> 40
            else -> 24
        }
        val expectedScore = 1.0 / (1.0 + 10.0.pow((opponentRating - myRating) / 400.0))
        val change = (kValue * (score - expectedScore)).toInt()
        val newRating = myRating + change;

        Log.d("!!!", "myRating-${myRating}, opponentRating-${opponentRating}, gamesPlayed-${gamesPlayed}, score-${score}, K-${kValue}, expectedScore-${expectedScore}, change-${change}, newRating-${newRating}")
        return newRating
    }


}