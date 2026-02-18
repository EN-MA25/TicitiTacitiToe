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

    // =============== Firestore initilize ===============
    private val firestore = FirebaseFirestore.getInstance()
    private val gameCollection = firestore.collection("game")

    // =============== State flow for online state ===============
    private val _onlineState = MutableStateFlow<OnlineGameState>(OnlineGameState())
    val onlineState: StateFlow<OnlineGameState> = _onlineState

    // =============== Listener reference ===============
    private var listenerRegistration: ListenerRegistration? = null

    fun startListenToMove(gameId: String?) {
        listenerRegistration = gameCollection.document(gameId!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists())
                    return@addSnapshotListener

            //    if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener

                // =============== Convert document to OnlineGameState ===============
                val game = snapshot.toObject(OnlineGameState::class.java)

                // =============== Update stateflow ===============
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
            onResult(Result.failure(Exception("Can't make move, GameID is missing")))
        }
        gameCollection.document(gameId!!).get().addOnSuccessListener { doc ->

            // =============== Convert Firestore document to OnlineGameState object ===============
            val game = doc.toObject(OnlineGameState::class.java)

            // =============== Get current player, uid for players and list of already made moves ===============
            var currentPlayer = game?.currentPlayerUid
            var playerX = game?.playerX
            var playerO = game?.playerO
            var moves = game!!.moves

            // =============== Check if box is already taken ===============
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

            // =============== Control if its the right player ===============
            if (move.player != currentPlayer) {
                onResult(
                    Result.failure(
                        Exception("You are not the current player!")
                    )
                )
            } else {

                // =============== Change player ===============
                if (currentPlayer == playerX) {
                    currentPlayer = playerO
                } else {
                    currentPlayer = playerX
                }

                val game = gameCollection.document(gameId!!)

                // =============== Add moves and updates current player ===============
                game.update("moves",FieldValue.arrayUnion(move))
                game.update("currentPlayerUid", currentPlayer)

                onResult(Result.success("success"))
            }
        }
    }

    fun getGameIfExist(currentUserId: String?, otherUserId: String?, onResult: (Result<String?>) -> Unit) {
        firestore.collection("game")
            // =============== Check id for players to see if theyre in a game & limit result to max 1 document  ===============
            .whereEqualTo("playerX", currentUserId)
            .whereEqualTo("playerO", otherUserId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->

                // =============== If game doesnt exist ===============
                if (documents.isEmpty) {

                    // =============== Check if players are saved in reversed order ===============
                    firestore.collection("game")
                        .whereEqualTo("playerX", otherUserId)
                        .whereEqualTo("playerO", currentUserId)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                onResult(Result.failure(Exception("Collection does not exist")))
                            }
                            else {
                                // =============== Collection exist ===============
                                for (document in documents) {
                                    onResult(Result.success(document.getString("gameId")))
                                }
                            }
                        }
                } else {
                    // =============== Collection exist ===============
                    for (document in documents) {
                        onResult(Result.success(document.getString("gameId")))
                    }
                }
            }
    }

    fun addOnlineGameResult(onlineGameResult: OnlineGameResult,
                            onResult: (Result<String>) -> Unit){

        val onlineGameResultId = firestore.collection("onlineGameResult").document().id

        val onlineGameResult = hashMapOf(
            "onlineGameResultId" to onlineGameResultId,
            "playerWhoWon" to onlineGameResult._playerWhoWon,
            "playerWhoLost" to onlineGameResult._playerWhoLost
        )
        firestore.collection("onlineGameResult")
            .document(onlineGameResultId)
            .set(onlineGameResult)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(onlineGameResultId))
                } else {
                    onResult(
                        Result.failure(
                            task.exception
                                ?: Exception("Failed to save")
                        )
                    )
                }
            }
    }

    fun createOnlineGame(
        playerX : String?,
        playerO: String?,
        startingPlayer: String?,
        onResult: (Result<String>) -> Unit
    ){
        // =============== New unique gameId ===============
        val gameId = firestore.collection("game").document().id

        // =============== Create new gameobject in Firestore ===============
        val game = hashMapOf(
            "gameId" to gameId,
            "playerX" to playerX,
            "playerO" to playerO,
            "currentPlayerUid" to startingPlayer,
            "gameResult" to "Ongoing",
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
