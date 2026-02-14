package com.example.ticititacititoe.onlinegame

import androidx.lifecycle.ViewModel
import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.Move
import com.example.ticititacititoe.game.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class OnlineGameViewModel : ViewModel() {

    private val repository = OnlineGameRepository()

    private val _uiState = MutableStateFlow(OnlineGameState())
    val uiState: StateFlow<OnlineGameState> = _uiState
    private var myUid: String? = null

    fun getGameIfExist(currentUserId: String?, otherUserId: String?, onResult: (Result<String?>) -> Unit) {
        repository.getGameIfExist(currentUserId, otherUserId){ result ->
            onResult(result)
        }
    }

    fun createOnlineGame(
        playerX : String?,
        playerO: String?,
        startingPlayer: String?,
        onResult: (Result<String>) -> Unit
    ){
        repository.createOnlineGame(playerX, playerO, startingPlayer) {
            result -> onResult(result)
        }
    }

    fun playerMakeMove(gameId: String?, row: Int, col: Int, playerUid: String, onResult: (Result<String>) -> Unit) {
        val onlineMove = OnlineMove(row, col, playerUid, gameId!!, System.currentTimeMillis())
        repository.playerMakeMove(
            gameId = gameId,
            move = onlineMove,
        ) { result ->
            if (result.isSuccess) {
                _uiState.update { state ->
                    if (state.board[row][col] != null) return@update state

                    val newBoard = copyBoard(state.board)
                    val newMoves = state.moves.toMutableList()

                    val currentPlayer = result.getOrNull()

                    if (currentPlayer == "playerX") {
                        state.currentPlayer = Player.X
                    } else {
                        state.currentPlayer = Player.O
                    }

                    val current = state.currentPlayer

                    // Add new move
                    newMoves += onlineMove
                    newBoard[row][col] = current

                    state.copy(
                        board = newBoard,
                        moves = newMoves,
                    )
                }

                onResult(result)
            }
        }
    }

    private fun copyBoard(
        board: Array<Array<Player?>>
    ): Array<Array<Player?>> {
        return Array(3) { r ->
            Array(3) { c -> board[r][c] }
        }
    }
}