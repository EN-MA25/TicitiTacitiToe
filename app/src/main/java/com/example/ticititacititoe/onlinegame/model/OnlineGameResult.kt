package com.example.ticititacititoe.onlinegame.model

class OnlineGameResult(
    val playerWhoWon: String? = "",
    val playerWhoLost: String? = "",
    val timestamp: Long = 0L,
    val movesMade: Int = 0,
)