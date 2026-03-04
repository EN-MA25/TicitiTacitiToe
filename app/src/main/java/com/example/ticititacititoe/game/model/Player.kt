package com.example.ticititacititoe.game.model

enum class Player {
    X, O;

    fun next(): Player = if (this == X) O else X
}