package com.example.ticititacititoe.onlinegame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

//        val state = _uiState.value
//
//        if (state.gameResult != "Ongoing") return
//        if (state.currentPlayerUid != myUid) return
//        if (state.board[row][col] != null) return

        val move = mapOf(
            "row" to row,
            "col" to col,
            "playerUid" to playerUid,
            "timestamp" to System.currentTimeMillis()
        )
        repository.playerMakeMove(
            gameId = gameId,
            move = move,
        ) {
            result -> onResult(result)
        }
    }
}