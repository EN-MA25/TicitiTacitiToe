package com.example.ticititacititoe.onlinegame.model

data class OnlineMove (
    var row: Long = 0L,
    var col: Long = 0L,
    var player: String? = "",
    var gameId: String = "",
    var timestamp: Long = System.currentTimeMillis()
)