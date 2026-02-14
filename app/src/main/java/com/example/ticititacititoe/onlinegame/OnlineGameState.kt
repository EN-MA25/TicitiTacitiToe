package com.example.ticititacititoe.onlinegame

import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.Move
import com.example.ticititacititoe.game.Player
import java.sql.Timestamp

data class OnlineGameState (
    val board: Array<Array<Player?>> = Array(3) { Array<Player?>(3) { null } },
    var currentPlayerUid: String? = "",
    var gameResult: String = "Ongoing",
    val moves: List<OnlineMove> = emptyList(),
    var playerX: String? = "",
    var playerO: String? = "",
    var startingPlayer: String? = "",
    var currentPlayer: Player = Player.X,
    val gameId: String = "",

    //val oldestMove: Pair<Int, Int>? = null

)
