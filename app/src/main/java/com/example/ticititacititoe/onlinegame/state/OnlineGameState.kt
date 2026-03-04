package com.example.ticititacititoe.onlinegame.state

import com.example.ticititacititoe.onlinegame.model.OnlineMove

data class OnlineGameState (
    var currentPlayerUid: String? = "",
    var gameResult: String = "Ongoing",
    var moves: MutableList<OnlineMove> = mutableListOf(),
    var playerX: String? = "",
    var playerO: String? = "",
    var playerLeftId: String? = "",
    var startingPlayer: String? = "",
    var gameId: String = "",
    var timestamp: Long = System.currentTimeMillis()
)