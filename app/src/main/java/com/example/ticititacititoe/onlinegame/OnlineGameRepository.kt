package com.example.ticititacititoe.onlinegame

import android.util.Log
import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.recentGame.RecentGame
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await


class OnlineGameRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val gameCollection = firestore.collection("game")

    private val _onlineState = MutableStateFlow<OnlineGameState>(OnlineGameState())

    val onlineState: StateFlow<OnlineGameState> = _onlineState

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

    suspend fun playerMakeMove(
        gameId: String?,
        move: OnlineMove,
    ): Result<String> {
        return try {

            if (gameId == null) {
                return Result.failure(Exception("Can't make move, game is missing"))
            }

            val doc = gameCollection
                .document(gameId)
                .get()
                .await()

            // =============== Convert Firestore document to OnlineGameState object ===============
            val gameState = doc.toObject(OnlineGameState::class.java)

            // ============== Stop players from making more moves =======
            if (gameState?.gameResult != "Ongoing") {
                return Result.failure(Exception("Game is already finished"))
            }

            // =============== Get current player, uid for players and list of already made moves ===============
            var currentPlayer = gameState.currentPlayerUid
            var playerX = gameState.playerX
            var playerO = gameState.playerO
           // var moves = gameState.moves

            // =============== Check if box is already taken ===============
            if (gameState.moves.takeLast(6).any {
                    it.row == move.row && it.col == move.col
                }) {
                return Result.failure(Exception("Already taken"))
            }

            // =============== Control if its the right player ===============
            if (move.player != currentPlayer) {
                return Result.failure(Exception("You are not the current user"))
            }

                // =============== Change player ===============
                if (currentPlayer == playerX) {
                    currentPlayer = playerO
                } else {
                    currentPlayer = playerX
                }

                val game = gameCollection.document(gameId!!)

                // =============== Add moves and updates current player ===============
                game.update("currentPlayerUid", currentPlayer)
                game.update(
                    "moves", FieldValue.arrayUnion(move)
                ).await()
                Result.success("success")

            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteGame(gameId: String?): Result<String> = try {
        Firebase.firestore.collection(FirestoreCollections.GAME)
            .document(gameId!!)
            .delete()
            .await()

        Result.success("deleted")
    } catch (e: Exception) {
        Result.failure(e)
    }

   suspend fun getGameIfExist(
        currentUserId: String?,
        otherUserId: String?,
    ): Result<String?> {
        return try {

            // =============== Check id for players to see if theyre in a game & limit result to max 1 document  ===============
            val gameSearch1 = firestore.collection(FirestoreCollections.GAME)
                .whereEqualTo("playerX", currentUserId)
                .whereEqualTo("playerO", otherUserId)
                .limit(1)
                .get()
                .await()

            // =============== If game doesnt exist ===============
            if (!gameSearch1.isEmpty) {
                val gameId = gameSearch1.first().getString("gameId")
                return Result.success(gameId)
            }

            // =============== Check if players are saved in reversed order ===============
            val gameSearch2 = firestore.collection(FirestoreCollections.GAME)
                .whereEqualTo("playerX", otherUserId)
                .whereEqualTo("playerO", currentUserId)
                .limit(1)
                .get()
                .await()

            if (!gameSearch2.isEmpty) {
                val gameId = gameSearch2.first().getString("gameId")
                return Result.success(gameId)
            }
            Result.failure(Exception("Collection does not exist"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOnlineGameResult(
        gameId: String,
        onlineGameResult: OnlineGameResult,
        timestamp: Long,
        movesMade: Int,
    ): Result<String> =
        try {

            val data = hashMapOf(
                "onlineGameResultId" to gameId,
                "playerWhoWon" to onlineGameResult.playerWhoWon,
                "playerWhoLost" to onlineGameResult.playerWhoLost,
                "timestamp" to timestamp,
                "movesMade" to movesMade
            )
            firestore.collection(FirestoreCollections.ONLINE_GAME_RESULT)
                .document(gameId)
                .set(data)
                .await()
            Result.success(gameId)
        } catch (e: Exception) {
            Result.failure(e)
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
        val gameId = firestore.collection(FirestoreCollections.GAME).document().id

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

        firestore.collection(FirestoreCollections.GAME)
            .document(gameId)
            .set(game)
            .await()

       Result.success(gameId!!)

    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getGameResult(gameId: String): OnlineGameResult? {
        val document = firestore.collection(FirestoreCollections.ONLINE_GAME_RESULT)
            .document(gameId)
            .get()
            .await()
        return document.toObject(OnlineGameResult::class.java)
    }

    suspend fun deleteRecentGame(gameId: String, userId: String) {
        firestore.collection("onlineGameResult")
            .document(gameId)
            .update("deletedBy", FieldValue.arrayUnion(userId))
            .await()
    }

    fun getRecentGames(userId: String, onResult: (Result<List<RecentGame>>) -> Unit) {
        Log.d("RecentGames", "FUNCTION CALLED with userId: $userId")

        val wonQuery = firestore.collection(FirestoreCollections.ONLINE_GAME_RESULT)
            .whereEqualTo("playerWhoWon", userId)

        val lostQuery = firestore.collection(FirestoreCollections.ONLINE_GAME_RESULT)
            .whereEqualTo("playerWhoLost", userId)

        wonQuery.get().addOnSuccessListener { wonDocs ->
            Log.d("RecentGames", "wonDocs count: ${wonDocs.size()}")
            lostQuery.get().addOnSuccessListener { lostDocs ->
                Log.d("RecentGames", "lostDocs count: ${lostDocs.size()}")

                val allDocs = (wonDocs.documents + lostDocs.documents)
                    .sortedByDescending { it.getLong("timestamp") ?: 0L }
                    .filter { doc ->
                      val deletedBy = doc.get("deletedBy") as? List<*> ?: emptyList<String>()
                        !deletedBy.contains(userId)
                    }
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

                    firestore.collection(FirestoreCollections.USERS)
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




