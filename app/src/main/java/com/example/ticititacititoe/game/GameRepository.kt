package com.example.ticititacititoe.game

import com.google.firebase.firestore.FirebaseFirestore

class GameRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun createGame(
        playerX : String,
        playerO: String,
        startingPlayer: String,
        currentPlayer: String,
        gameResult: String,
        onResult: (Result<String>) -> Unit
    ){
        val gameId = firestore.collection("game").document().id

        val game = hashMapOf(
            "gameId" to gameId,
            "playerX" to playerX,
            "playerO" to playerO,
            "startingPlayer" to startingPlayer,
            "currentPlayer" to currentPlayer,
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