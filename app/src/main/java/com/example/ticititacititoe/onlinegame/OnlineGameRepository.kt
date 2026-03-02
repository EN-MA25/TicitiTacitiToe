package com.example.ticititacititoe.onlinegame

import android.util.Log
import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.recentGame.RecentGame
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await


class OnlineGameRepository {

    // =============== Firestore initilize ===============
    private val firestore = FirebaseFirestore.getInstance()
    private val gameCollection = firestore.collection("game")

    // =============== State flow for online state ===============
    private val _onlineState = MutableStateFlow<OnlineGameState>(OnlineGameState())
    val onlineState: StateFlow<OnlineGameState> = _onlineState

    // =============== Listener reference ===============
    private var listenerRegistration: ListenerRegistration? = null

    var moveCount: Int = 0

    fun startListenToMove(gameId: String?) {
        listenerRegistration = gameCollection.document(gameId!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists())
                    return@addSnapshotListener

                // =============== Convert document to OnlineGameState ===============
                val game = snapshot.toObject(OnlineGameState::class.java)

                if (game?.playerLeftId != "") {
                    _onlineState.value = game!!
                    return@addSnapshotListener
                }

                if (moveCount == game!!.moves.count())
                    return@addSnapshotListener

                moveCount = game.moves.count()


                // =============== Update stateflow ===============
                _onlineState.value = game!!
            }
    }

    fun removeListener() {
        listenerRegistration?.remove()
    }

    // ============== Update gameresult =========
    fun updateGameResult(gameId: String, result: String) {
        gameCollection.document(gameId)
            .update("gameResult", result)
    }

    fun playerMakeMove(
        gameId: String?,
        move: OnlineMove,
        onResult: (Result<String>) -> Unit
    ) {
        if (gameId == null) {
            onResult(Result.failure(Exception("Can't make move, GameID is missing")))
            return
        }

        gameCollection.document(gameId).get().addOnSuccessListener { doc ->

            // =============== Convert Firestore document to OnlineGameState object ===============
            val game = doc.toObject(OnlineGameState::class.java)


            // ============== Stop players from making more moves =======
            if (game?.gameResult != "Ongoing") {
                onResult(Result.failure(Exception("Game is already finished")))
                return@addOnSuccessListener
            }

            // =============== Get current player, uid for players and list of already made moves ===============
            var currentPlayer = game.currentPlayerUid
            var playerX = game.playerX
            var playerO = game.playerO
            var moves = game.moves

            // =============== Check if box is already taken ===============
            for (madeMove in moves.takeLast(6)) {
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
                game.update("currentPlayerUid", currentPlayer)
                game.update("moves", FieldValue.arrayUnion(move))


                onResult(Result.success("success"))
            }
        }
    }

    fun deleteOldestMove(gameId: String?, moves: MutableList<OnlineMove>) {

        val game = gameCollection.document(gameId!!)

        game.update("moves", moves)

    }

    fun deleteGame(gameId: String?, onResult: (Result<String>) -> Unit) {

        gameCollection.document(gameId!!).delete()
            .addOnSuccessListener {
                onResult(Result.success("Game is deleted"))
            }
    }

    fun getGameIfExist(
        currentUserId: String?,
        otherUserId: String?,
        onResult: (Result<String?>) -> Unit
    ) {
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
                            } else {
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

    fun addOnlineGameResult(
        gameId: String,
        onlineGameResult: OnlineGameResult,
        timestamp: Long,
        movesMade: Int,
        onResult: (Result<String>) -> Unit
    ) {


        val onlineGameResult = hashMapOf(
            "onlineGameResultId" to gameId,
            "playerWhoWon" to onlineGameResult.playerWhoWon,
            "playerWhoLost" to onlineGameResult.playerWhoLost,
            "timestamp" to timestamp,
            "movesMade" to movesMade
        )
        firestore.collection("onlineGameResult")
            .document(gameId)
            .set(onlineGameResult)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(Result.success(gameId))
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

    fun userHasLeft(gameId: String?, userId: String?) {
        gameCollection.document(gameId!!)
            .update("playerLeftId", userId)
    }

   suspend fun createOnlineGame(
        playerX: String?,
        playerO: String?,
        startingPlayer: String?
    ): Result<String?> = try {
        // =============== New unique gameId ===============
        val gameId = firestore.collection("game").document().id

        // =============== Create new gameobject in Firestore ===============
        val game = hashMapOf(
            "gameId" to gameId,
            "playerX" to playerX,
            "playerO" to playerO,
            "currentPlayerUid" to startingPlayer,
            "gameResult" to "Ongoing",
            "playerLeftId" to "",
            "timestamp" to System.currentTimeMillis(),
            "moves" to emptyList<List<OnlineMove>>()
        )

        firestore.collection("game")
            .document(gameId)
            .set(game)
            .await()

       Result.success(gameId!!)

    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun getGameResult(gameId: String): OnlineGameResult? {
        val document = firestore.collection("onlineGameResult")
            .document(gameId)
            .get()
            .await()
        return document.toObject(OnlineGameResult::class.java)
    }


    fun getRecentGames(userId: String, onResult: (Result<List<RecentGame>>) -> Unit) {
        Log.d("RecentGames", "FUNCTION CALLED with userId: $userId")

        val wonQuery = firestore.collection("onlineGameResult")
            .whereEqualTo("playerWhoWon", userId)


        val lostQuery = firestore.collection("onlineGameResult")
            .whereEqualTo("playerWhoLost", userId)


        wonQuery.get().addOnSuccessListener { wonDocs ->
            Log.d("RecentGames", "wonDocs count: ${wonDocs.size()}")
            lostQuery.get().addOnSuccessListener { lostDocs ->
                Log.d("RecentGames", "lostDocs count: ${lostDocs.size()}")

                val allDocs = (wonDocs.documents + lostDocs.documents)
                    .sortedByDescending { it.getLong("timestamp") ?: 0L }
                    .take(5)

                if (allDocs.isEmpty()) {
                    onResult(Result.success(emptyList()))
                    return@addOnSuccessListener
                }

                val recentGames = mutableListOf<RecentGame>()
                var completedCount = 0

                for (doc in allDocs) {
                    val playerWhoWon = doc.getString("playerWhoWon") ?: ""
                    val playerWhoLost = doc.getString("playerWhoLost") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val movesMade = doc.getLong("movesMade")?.toInt() ?: 0
                    val opponentId = if (playerWhoWon == userId) playerWhoLost else playerWhoWon
                    val result = if (playerWhoWon == userId) "Won" else "Lost"

                    firestore.collection("users")
                        .document(opponentId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val username = userDoc.getString("username") ?: "Unknown"
                            recentGames.add(
                                RecentGame(doc.id, opponentId, username, result, timestamp, movesMade)
                            )
                            completedCount++
                            if (completedCount == allDocs.size) {
                                onResult(Result.success(recentGames))
                            }
                        }
                        .addOnFailureListener {
                            completedCount++
                            if (completedCount == allDocs.size) {
                                onResult(Result.success(recentGames))
                            }
                        }
                }
            }.addOnFailureListener { exception ->
                Log.e("RecentGames", "lostQuery failed: ${exception.message}")
                onResult(Result.failure(exception))
            }
        }.addOnFailureListener { exception ->
            Log.e("RecentGames", "wonQuery failed: ${exception.message}")
            onResult(Result.failure(exception))
        }
    }
}




