package com.example.ticititacititoe.onlinegame

import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.Move
import com.example.ticititacititoe.game.Player
import java.sql.Timestamp

data class OnlineGameState (
    val board: Array<Array<String?>> = Array(3) { Array<String?>(3) { null } },
    val currentPlayerUid: String = "",
    val gameResult: String = "Ongoing",
    val moves: List<OnlineMove> = emptyList(),
    val playerX: String = "",
    val playerO: String = "",
    val startingPlayer: String = "",
    val gameId: String = "",

    //val oldestMove: Pair<Int, Int>? = null

)
