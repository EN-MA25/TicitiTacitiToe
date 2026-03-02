package com.example.ticititacititoe.onlinegame

class OnlineGameLogic {

    private val winningPositions = listOf(
        listOf(0 to 0, 0 to 1, 0 to 2),
        listOf(1 to 0, 1 to 1, 1 to 2),
        listOf(2 to 0, 2 to 1, 2 to 2),
        listOf(0 to 0, 1 to 0, 2 to 0),
        listOf(0 to 1, 1 to 1, 2 to 1),
        listOf(0 to 2, 1 to 2, 2 to 2),
        listOf(0 to 0, 1 to 1, 2 to 2),
        listOf(0 to 2, 1 to 1, 2 to 0)
    )

    fun checkWinner(state: OnlineGameState): String? {
        val playerXMoves = state.moves.filter { it.player == state.playerX }
        val playerOMoves = state.moves.filter { it.player == state.playerO }

        for (combo in winningPositions) {
            if (combo.all { (r, c) -> playerXMoves.any { it.row.toInt() == r && it.col.toInt() == c } }) {
                return state.playerX
            }
            if (combo.all { (r, c) -> playerOMoves.any { it.row.toInt() == r && it.col.toInt() == c } }) {
                return state.playerO
            }
        }
        return null
    }
}