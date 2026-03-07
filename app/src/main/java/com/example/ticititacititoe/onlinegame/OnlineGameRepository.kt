package com.example.ticititacititoe.onlinegame

import android.util.Log
import com.example.ticititacititoe.game.recentGame.RecentGame
import com.example.ticititacititoe.onlinegame.constant.FirestoreCollections
import com.example.ticititacititoe.onlinegame.model.OnlineGameResult
import com.example.ticititacititoe.onlinegame.model.OnlineMove
import com.example.ticititacititoe.onlinegame.state.OnlineGameState
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

                // =============== If playerLeftId field is not empty, return ===============
                if (game?.playerLeftId != "") {
                    _onlineState.value = game!!
                    return@addSnapshotListener
                }

                // =============== If moves hasn't changed, return ===============
                if (moveCount == game!!.moves.count())
                    return@addSnapshotListener

                // =============== If moves has changed, update ===============
                moveCount = game.moves.count()

                // =============== Update stateflow ===============
                _onlineState.value = game!!
            }
    }

    fun removeListener() {
        listenerRegistration?.remove()
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

            // =============== Check if box is already taken ===============
            if (gameState.moves.takeLast(6).any {
                    it.row == move.row && it.col == move.col
                }) {
                return Result.failure(Exception("Already taken"))
            }

            // =============== Control if it's the right player ===============
            if (move.player != currentPlayer) {
                return Result.failure(Exception("It is NOT your turn!!!°"))
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

            // =============== Check id for players to see if they're in a game & limit result to max 1 document  ===============
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

    suspend fun getRecentGames(userId: String): Result<List<RecentGame>> {
        return try {
            val wonDocs = firestore.collection(FirestoreCollections.ONLINE_GAME_RESULT)
                .whereEqualTo("playerWhoWon", userId)
                .get()
                .await()

            val lostDocs = firestore.collection(FirestoreCollections.ONLINE_GAME_RESULT)
                .whereEqualTo("playerWhoLost", userId)
                .get()
                .await()

            val allDocs = (wonDocs.documents + lostDocs.documents)
                .sortedByDescending { it.getLong("timestamp") ?: 0L }
                .filter { doc ->
                    val deletedBy = doc.get("deletedBy") as? List<*> ?: emptyList<String>()
                    !deletedBy.contains(userId)
                }
                .take(5)

            if (allDocs.isEmpty()) {
                return Result.success(emptyList())
            }

            val recentGames = mutableListOf<RecentGame>()

            for (doc in allDocs) {
                val playerWhoWon = doc.getString("playerWhoWon") ?: ""
                val playerWhoLost = doc.getString("playerWhoLost") ?: ""
                val timestamp = doc.getLong("timestamp") ?: 0L
                val movesMade = doc.getLong("movesMade")?.toInt() ?: 0
                val opponentId = if (playerWhoWon == userId) playerWhoLost else playerWhoWon
                val result = if (playerWhoWon == userId) "Won" else "Lost"

                val userDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(opponentId)
                    .get()
                    .await()

                val username = userDoc.getString("username") ?: "Unknown"
                recentGames.add(
                    RecentGame(doc.id, opponentId, username, result, timestamp, movesMade)
                )
            }

            Result.success(recentGames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}




