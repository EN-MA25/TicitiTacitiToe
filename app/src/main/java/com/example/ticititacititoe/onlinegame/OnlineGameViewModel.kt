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

    // =========== Online state ===========
    val onlineState: StateFlow<OnlineGameState> = repository.onlineState
    fun startListenToMove(gameId: String) {
        repository.startListenToMove(gameId)
    }

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
    fun playerMakeMove(gameId: String?, row: Long, col: Long, playerUid: String?, onResult: (Result<String>) -> Unit) {

        // =========== Create onlinemove object=============
        val onlineMove = OnlineMove(row, col, playerUid, gameId!!, System.currentTimeMillis())

        // =========== Send move to repository ===========
        repository.playerMakeMove(
            gameId = gameId,
            move = onlineMove,
        ) { result ->
            if (result.isSuccess) {
                onResult(result)
            }
        }
    }

    fun updateGameResult(gameId: String, result: String) {
        repository.updateGameResult(gameId, result)
    }

    fun deleteGame(gameId: String?, onResult: (Result<String>) -> Unit) {
        repository.deleteGame(gameId){ result ->
            onResult(result)

        }
    }

    fun userHasLeft(gameId: String?, userId: String?) {
        repository.userHasLeft(gameId, userId)
    }

    fun addOnlineGameResult(onlineGameResult: OnlineGameResult, onResult: (Result<String>) -> Unit){
        repository.addOnlineGameResult(onlineGameResult){ result ->
            onResult(result)
        }
    }

    // =========== Clear ===========
    override fun onCleared() {
        super.onCleared()
        repository.removeListener()
    }
}