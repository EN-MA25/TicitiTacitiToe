package com.example.ticititacititoe.game

data class Move(
    val row: Int,
    val col: Int,
    val player: Player,
    val moveIndex: Long
)
