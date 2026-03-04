package com.example.ticititacititoe.onlinegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.Move
import com.example.ticititacititoe.game.Player
import com.example.ticititacititoe.game.recentGame.RecentGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnlineGameViewModel : ViewModel() {

    private val repository = OnlineGameRepository()

    private val _gameResult = MutableStateFlow<OnlineGameResult?>(null)
    val gameResult = _gameResult.asStateFlow()
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

    suspend fun createOnlineGame(
        playerX : String?,
        playerO: String?,
        startingPlayer: String?
    ): Result<String?> {

        return repository.createOnlineGame(playerX, playerO, startingPlayer)
    }

   suspend fun playerMakeMove(gameId: String?, row: Long, col: Long, playerUid: String?): Result <String> {

        // =========== Create onlinemove object=============
        val onlineMove = OnlineMove(row, col, playerUid, gameId!!, System.currentTimeMillis())

        // =========== Send move to repository ===========
       return repository.playerMakeMove(
            gameId = gameId,
            move = onlineMove,
        )
    }

    fun updateGameResult(gameId: String, result: String) {
        repository.updateGameResult(gameId, result)
    }

    suspend fun deleteGame(gameId: String?): Result <String> {
       return repository.deleteGame(gameId)
    }

    fun userHasLeft(gameId: String?, userId: String?) {
        repository.userHasLeft(gameId, userId)
    }

    suspend fun addOnlineGameResult(gameId: String, onlineGameResult: OnlineGameResult,timestamp: Long, movesMade: Int
    ): Result<String> {
        return repository.addOnlineGameResult(
            gameId,
            onlineGameResult,
            timestamp ,
            movesMade)
    }

    override fun onCleared() {
        super.onCleared()
        repository.removeListener()
    }

    fun fetchRecentGames(userId: String, onResult: (Result<List<RecentGame>>)-> Unit){
        repository.getRecentGames(userId, onResult)
    }

        fun fetchGameResult(gameId: String) {
            viewModelScope.launch {
                try {
                    val result = repository.getGameResult(gameId)
                    _gameResult.value = result
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }