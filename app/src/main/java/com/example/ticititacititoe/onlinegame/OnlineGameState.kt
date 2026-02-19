package com.example.ticititacititoe.onlinegame

import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.Move
import com.example.ticititacititoe.game.Player
import java.sql.Timestamp

data class OnlineGameState (
    var currentPlayerUid: String? = "",
    var gameResult: String = "Ongoing",
    var moves: List<OnlineMove> = emptyList(),
    var playerX: String? = "",
    var playerO: String? = "",
    var startingPlayer: String? = "",
    var gameId: String = "",
    var timestamp: Long = System.currentTimeMillis()

    //val oldestMove: Pair<Int, Int>? = null

)
