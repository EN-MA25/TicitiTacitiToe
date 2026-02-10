package com.example.ticititacititoe.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GameViewModel: ViewModel() {
    // Speaking to GameRepository and GameActivity
    private val maxPiecesPerPlayer = 3
    private var globalMoveCounter = 0L

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState

    fun makeMove(row: Int, col: Int) {
        _uiState.update { state ->
            if (state.gameResult != GameResult.Ongoing) return@update state
            if (state.board[row][col] != null) return@update state

            val newBoard = copyBoard(state.board)
            val newMoves = state.moves.toMutableList()
            val current = state.currentPlayer

            // Add new move
            val move = Move(row, col, current, globalMoveCounter++)
            newMoves += move
            newBoard[row][col] = current

            // Enforce max 3 pieces per player (FIFO)
            val playerMoves = newMoves.filter { it.player == current }
            if (playerMoves.size > maxPiecesPerPlayer) {
                val oldest = playerMoves.minBy { it.moveIndex }
                newBoard[oldest.row][oldest.col] = null
                newMoves.remove(oldest)
            }

            val win = checkWin(newBoard, current)

            val oldestMove = findOldestMove(newMoves)

            state.copy(
                board = newBoard,
                moves = newMoves,
                currentPlayer = if (win == GameResult.Ongoing) current.next() else current,
                gameResult = win,
                oldestMove = oldestMove

            )
        }
    }

    fun resetGame() {
        globalMoveCounter = 0
        _uiState.value = GameState()
    }

    // -------------------------
    // Win Checking (3x3)
    // -------------------------

    private fun checkWin(
        board: Array<Array<Player?>>,
        player: Player
    ): GameResult {
        val lines = listOf(
            // Rows
            listOf(0 to 0, 0 to 1, 0 to 2),
            listOf(1 to 0, 1 to 1, 1 to 2),
            listOf(2 to 0, 2 to 1, 2 to 2),

            // Cols
            listOf(0 to 0, 1 to 0, 2 to 0),
            listOf(0 to 1, 1 to 1, 2 to 1),
            listOf(0 to 2, 1 to 2, 2 to 2),

            // Diagonals
            listOf(0 to 0, 1 to 1, 2 to 2),
            listOf(0 to 2, 1 to 1, 2 to 0)
        )

        for (line in lines) {
            if (line.all { (r, c) -> board[r][c] == player }) {
                return GameResult.Win(player)
            }
        }

        return GameResult.Ongoing
    }

    private fun findOldestMove(
        moves: List<Move>
    ): Pair<Int, Int>? {
        if (globalMoveCounter < 6) {
            return null
        }
        return moves
            .minByOrNull { it.moveIndex }
            ?.let { it.row to it.col }
    }

    private fun copyBoard(
        board: Array<Array<Player?>>
    ): Array<Array<Player?>> {
        return Array(3) { r ->
            Array(3) { c -> board[r][c] }
        }
    }

}