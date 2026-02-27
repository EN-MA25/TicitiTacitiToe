package com.example.ticititacititoe.profile

data class User(
    val id: String = "",
    val username: String? = "",
    val email: String = "",
    val rating: Int = 1300,
    val totalGames: Int = 0,
    val wonGames: Int = 0,
    val lostGames: Int = 0,
    val totalMovesMade: Int = 0,
    val maxStreak: Int = 0,
    val currentStreak: Int = 0
) {
    val winRate: Int
        get() = if (totalGames > 0) {
            ((wonGames.toDouble() / totalGames.toDouble()) * 100).toInt()
        } else 0
}
