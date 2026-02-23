package com.example.ticititacititoe.onlinegame

import com.example.ticititacititoe.game.Player
import java.sql.Timestamp

data class OnlineMove (
    var row: Long = 0L,
    var col: Long = 0L,
    var player: String? = "",
    var gameId: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
