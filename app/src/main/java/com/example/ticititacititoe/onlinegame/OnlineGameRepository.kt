package com.example.ticititacititoe.onlinegame

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class OnlineGameRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val gameCollection = firestore.collection("game")

    fun playerMakeMove(
        gameId: String?,
        move: OnlineMove,
        onResult: (Result<String>) -> Unit
    ){
        if (gameId == null) {
            //TODO: handle error
        }
        gameCollection.document(gameId!!).get().addOnSuccessListener { doc ->
            var currentPlayer = doc.getString("currentPlayer")
            val playerX = doc.getString("playerX")
            val playerO = doc.getString("playerO")
            val moves = doc.get("moves") as? ArrayList<HashMap<String, Any>> ?: emptyList()

            for (madeMove in moves) {
                if ((madeMove.get("row") as Long).toInt() == move.row && (madeMove.get("col") as Long).toInt() == move.col) {
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

                gameCollection.document(gameId!!)
                    .update(
                        mapOf(
                            "moves" to FieldValue.arrayUnion(move),
                            "currentPlayer" to currentPlayer
                        )
                    )
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
            "currentPlayer" to startingPlayer,
            "gameResult" to "",
            "timestamp" to System.currentTimeMillis(),
            "moves" to emptyList<ArrayList<OnlineMove>>()
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
