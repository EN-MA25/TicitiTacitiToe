package com.example.ticititacititoe.game

sealed class GameResult {
    object Ongoing : GameResult()
    data class Win(val winner: Player) : GameResult()
    object Draw : GameResult()
}