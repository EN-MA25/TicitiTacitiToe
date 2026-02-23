package com.example.ticititacititoe.game.recentGame

data class RecentGame (
    val gameId: String,
    val opponentId: String,
    val opponentUsername: String,
    val result: String,
    val timestamp: Long,
    val movesMade: Int
)