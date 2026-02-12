package com.example.ticititacititoe.onlinegame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnlineGameViewModel : ViewModel() {

    private val repository = OnlineGameRepository()

    private val _uiState = MutableStateFlow(OnlineGameState())
    val uiState: StateFlow<OnlineGameState> = _uiState
    private var myUid: String? = null

    fun setMyUid(uid: String){
        myUid = uid
    }

    fun playerMakeMove(row: Int, col: Int, playerUid: String) {

        val state = _uiState.value

        if (state.gameResult != "Ongoing") return
        if (state.currentPlayerUid != myUid) return
        if (state.board[row][col] != null) return

        val move = mapOf(
            "row" to row,
            "col" to col,
            "playerUid" to myUid,
            "timestamp" to System.currentTimeMillis()
        )
        repository.playerMakeMove(
            gameId = state.gameId,
            move = move,
            playerUid = playerUid
        )
    }
}