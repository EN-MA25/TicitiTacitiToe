package com.example.ticititacititoe.onlinegame

import kotlin.text.toInt

class OnlineGameLogic {

fun checkWinner(state: OnlineGameState): Boolean {

        //Get player from state
        val playerX = state.playerX

        val playerXMoves = mutableListOf<OnlineMove>()
        val playerOMoves = mutableListOf<OnlineMove>()

        for (move in state.moves.takeLast(6)) {
            // Add moves in playerWMoves if player is playerX
            if (playerX == move.player) {
                playerXMoves.add(move)
            } else {
                playerOMoves.add(move)
            }
        }

        val winningPositions = listOf(
            listOf(0 to 0, 0 to 1, 0 to 2),
            listOf(1 to 0, 1 to 1, 1 to 2),
            listOf(2 to 0, 2 to 1, 2 to 2),
            listOf(0 to 0, 1 to 0, 2 to 0),
            listOf(0 to 1, 1 to 1, 2 to 1),
            listOf(0 to 2, 1 to 2, 2 to 2),
            listOf(0 to 0, 1 to 1, 2 to 2),
            listOf(0 to 2, 1 to 1, 2 to 0)
        )
        for (combinations in winningPositions) {
            if (combinations.all { (r, c) -> playerXMoves.any { it.row.toInt() == r && it.col.toInt() == c && state.gameResult == "Ongoing" } }) {
                state.gameResult = "Player X won"
                return true
            }
        }

        for (combo in winningPositions) {
            if (combo.all { (r, c) -> playerOMoves.any { it.row.toInt() == r && it.col.toInt() == c && state.gameResult == "Ongoing" } }) {
                state.gameResult = "Player O won"
                return true
            }
        }
        return false
    }
}
