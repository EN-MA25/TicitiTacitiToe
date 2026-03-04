package com.example.ticititacititoe.game.model

sealed class GameResult {
    object Ongoing : GameResult()
    data class Win(val winner: Player) : GameResult()
    object Draw : GameResult()
}