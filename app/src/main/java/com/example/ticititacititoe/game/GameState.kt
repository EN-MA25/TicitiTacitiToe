package com.example.ticititacititoe.game

data class GameState(
    val board: Array<Array<Player?>> = Array(3) { Array<Player?>(3) { null } },
    val currentPlayer: Player = Player.X,
    val gameResult: GameResult = GameResult.Ongoing,
    val moves: List<Move> = emptyList(),

    val oldestMove: Pair<Int, Int>? = null

)
