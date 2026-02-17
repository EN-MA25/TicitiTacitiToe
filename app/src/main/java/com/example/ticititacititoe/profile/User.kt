package com.example.ticititacititoe.profile

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val rating: Double = 0.0,
    val totalGames: Int = 0,
    val wonGames: Int = 0,
    val lostGames: Int = 0,
    val maxStreak: Int = 0,
    val currentStreak: Int = 0
) {
}
