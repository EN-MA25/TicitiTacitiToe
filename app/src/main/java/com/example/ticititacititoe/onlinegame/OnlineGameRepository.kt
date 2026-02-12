package com.example.ticititacititoe.onlinegame

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class OnlineGameRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val gameCollection = firestore.collection("game")

    fun playerMakeMove(
        gameId: String,
        move: Map<String, Any?>,
        playerUid: String
    ){
        gameCollection.document(gameId)
            .update(
                mapOf(
                    "moves" to FieldValue.arrayUnion(move),
                    "player" to playerUid
                )
            )
    }

    fun createOnlineGame(
        playerX : String,
        playerO: String,
        startingPlayer: String,
        player: String,
        gameResult: String,
        onResult: (Result<String>) -> Unit
    ){
        val gameId = firestore.collection("game").document().id

        val game = hashMapOf(
            "gameId" to gameId,
            "playerX" to playerX,
            "playerO" to playerO,
            "startingPlayer" to startingPlayer,
            "player" to player,
            "gameResult" to gameResult,
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
