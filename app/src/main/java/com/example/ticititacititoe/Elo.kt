package com.example.ticititacititoe

import kotlin.math.min
import kotlin.math.pow

object Elo {

    fun expectedScore(myRating: Int, opponentRating: Int): Double {
        return 1.0 / (1.0 + 10.0.pow((myRating - opponentRating) / 400.0))
    }

    fun newRating(myRating: Int, opponentRating: Int, gamesPlayed: Int, score: Double): Int {
        val kValue = when {
            gamesPlayed <= 10 -> 60
            gamesPlayed <= 30 -> 40
            else -> 24
        }
        val expectedScore = expectedScore(myRating, opponentRating)
        val change = (kValue * (score - expectedScore)).toInt()
        //val maxChange = maxOf(-30, minOf(30,change))
        val newRating = myRating + change;
        return newRating
    }
}