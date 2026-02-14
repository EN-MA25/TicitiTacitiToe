package com.example.ticititacititoe.onlinegame

import android.util.Log
import com.example.ticititacititoe.game.InviteState
import com.example.ticititacititoe.game.Player
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnlineGameRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val gameCollection = firestore.collection("game")

    private val _onlineState = MutableStateFlow<OnlineGameState>(OnlineGameState())
    val onlineState: StateFlow<OnlineGameState> = _onlineState

    private var listenerRegistration: ListenerRegistration? = null

    fun startListenToMove(gameId: String?) {
        listenerRegistration = gameCollection.document(gameId!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val game = snapshot.toObject(OnlineGameState::class.java)

                _onlineState.value = game!!
            }
    }

    fun removeListener() {
        listenerRegistration?.remove()
    }

    fun playerMakeMove(
        gameId: String?,
        move: OnlineMove,
        onResult: (Result<String>) -> Unit
    ){
        if (gameId == null) {
            //TODO: handle error
        }
        gameCollection.document(gameId!!).get().addOnSuccessListener { doc ->
            val game = doc.toObject(OnlineGameState::class.java)
            var currentPlayer = game?.currentPlayerUid
            var playerX = game?.playerX
            var playerO = game?.playerO
            var moves = game!!.moves

            for (madeMove in moves) {
                if (madeMove.row == move.row && madeMove.col == move.col) {
                    onResult(
                        Result.failure(
                            Exception("Already taken")
                        )
                    )
                    return@addOnSuccessListener
                }
            }

            if (move.player != currentPlayer) {
                onResult(
                    Result.failure(
                        Exception("You are not the current player!")
                    )
                )
            }
            else {
                var playerResult = ""
                if (currentPlayer == playerX) {
                    currentPlayer = playerO
                    playerResult = "playerX"
                } else
                {
                    currentPlayer = playerX
                    playerResult = "playerO"
                }

                val game = gameCollection.document(gameId!!)
                game.update("moves",FieldValue.arrayUnion(move))
                game.update("currentPlayerUid", currentPlayer)

                onResult(Result.success(playerResult))
            }
        }
    }

    fun getGameIfExist(currentUserId: String?, otherUserId: String?, onResult: (Result<String?>) -> Unit) {
        firestore.collection("game")
            .whereEqualTo("playerX", currentUserId)
            .whereEqualTo("playerO", otherUserId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("FirestoreCheck", "Collection does not exist")
                    firestore.collection("game")
                        .whereEqualTo("playerX", otherUserId)
                        .whereEqualTo("playerO", currentUserId)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                Log.d("FirestoreCheck", "Collection does not exist")
                                onResult(Result.failure(Exception("Collection does not exist")))
                            }
                            else {
                                //collection exist!
                                for (document in documents) {
                                    onResult(Result.success(document.getString("gameId")))
                                }
                            }
                        }
                } else {
                    // Collection exists
                    Log.d("FirestoreCheck", "Collection exists")
                    for (document in documents) {
                        onResult(Result.success(document.getString("gameId")))
                    }
                }
            }
    }

    fun createOnlineGame(
        playerX : String?,
        playerO: String?,
        startingPlayer: String?,
        onResult: (Result<String>) -> Unit
    ){
        val gameId = firestore.collection("game").document().id
        val game = hashMapOf(
            "gameId" to gameId,
            "playerX" to playerX,
            "playerO" to playerO,
            "currentPlayerUid" to startingPlayer,
            "gameResult" to "",
            "timestamp" to System.currentTimeMillis(),
            "moves" to emptyList<List<OnlineMove>>()
        )

        firestore.collection("game")
            .document(gameId)
            .set(game)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(gameId))
                } else {
                    onResult(
                        Result.failure(
                            task.exception
                                ?: Exception("Game failed to start")
                        )
                    )

                }
            }
    }
}
