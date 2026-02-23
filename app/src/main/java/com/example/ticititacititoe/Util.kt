package com.example.ticititacititoe

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.pow

object Util {

    fun formatTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

    fun newRating(myRating: Int, opponentRating: Int, gamesPlayed: Int, score: Double): Int {
        val kValue = when {
            gamesPlayed <= 10 -> 60
            gamesPlayed <= 30 -> 40
            else -> 24
        }
        val expectedScore = 1.0 / (1.0 + 10.0.pow((myRating - opponentRating) / 400.0))
        val change = (kValue * (score - expectedScore)).toInt()
        val newRating = myRating + change;
        return newRating
    }


}