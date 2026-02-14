package com.example.ticititacititoe.onlinegame

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class OnlineGameRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val gameCollection = firestore.collection("game")

    fun playerMakeMove(
        gameId: String?,
        move: Map<String, Any?>,
        onResult: (Result<String>) -> Unit
    ){
        if (gameId == null) {
            //TODO: handle error
        }
        gameCollection.document(gameId!!).get().addOnSuccessListener { doc ->
            var currentPlayer = doc.getString("currentPlayer")
            val playerX = doc.getString("playerX")
            val playerO = doc.getString("playerO")

            if (move.get("playerUid") != currentPlayer) {
                onResult(
                    Result.failure(
                        Exception("You are not the current player!")
                    )
                )
            }

            currentPlayer = if (currentPlayer == playerX) playerO else playerX

            gameCollection.document(gameId!!)
                .update(
                    mapOf(
                        "moves" to FieldValue.arrayUnion(move),
                        "currentPlayer" to currentPlayer
                    )
                )
        }
    }

    fun getGameIfExist(currentUserId: String?, otherUserId: String?, onResult: (Result<String?>) -> Unit) {

        firestore.collection("game")
            .whereEqualTo("playerX", currentUserId)
            .whereEqualTo("playerO", otherUserId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    onResult(Result.success(document.getString("gameId")))
                }
            }
            .addOnFailureListener { exception ->
                firestore.collection("game")
                    .whereEqualTo("playerX", otherUserId)
                    .whereEqualTo("playerO", currentUserId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { documents ->
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
            "currentPlayer" to startingPlayer,
            "gameResult" to "",
            "timestamp" to System.currentTimeMillis(),
            "moves" to emptyList<Map<String, Any>>()
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
