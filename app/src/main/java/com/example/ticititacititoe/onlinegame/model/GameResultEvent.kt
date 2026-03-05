package com.example.ticititacititoe.onlinegame.model

import com.example.ticititacititoe.user.model.User

data class GameResultEvent(
    val newAchievements: List<String>,
    val user: User,
    val opponentUsername: String,
    val opponentUserId: String,
    val didWin: Boolean
)
