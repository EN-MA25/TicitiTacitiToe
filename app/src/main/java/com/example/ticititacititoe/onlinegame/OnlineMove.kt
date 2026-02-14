package com.example.ticititacititoe.onlinegame

import com.example.ticititacititoe.game.Player
import java.sql.Timestamp

data class OnlineMove (
    val row: Int,
    val col: Int,
    val player: String,
    val gameId: String,
    val timestamp: Long = System.currentTimeMillis()
)
