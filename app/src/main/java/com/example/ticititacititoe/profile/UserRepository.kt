package com.example.ticititacititoe.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ticititacititoe.Util
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = Firebase.firestore
    private val auth = Firebase.auth


    suspend fun searchUsers(searchTerm: String, currentUserId: String): List<User>{
        val snapshot = db.collection("users")
            .orderBy("username")
            .startAt(searchTerm)
            .endAt(searchTerm + "\uf8ff")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }.filter { user ->
            user.id != currentUserId
        }

    }

    suspend fun getAllUsers(): List<User> {
        val snapshot = db.collection("users")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }

    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)?.copy(id = doc.id)

    }
        fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid


//    suspend fun getUserDetailsById(userId: String): User? {
//        val document = db.collection("users")
//            .document(userId)
//            .get()
//            .await()
//
//        return document.toObject(User::class.java)?.copy(id = document.id)
//
//    }

    fun getUserDetailsById(userId: String, callback: (User?) -> Unit) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user?.copy(id = document.id))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null)
            }
    }

    fun updateUserAfterGame(me: User, opponent: User, didWin: Boolean, movesMade: Int) {

        val newRating = Util.newRating(me.rating, opponent.rating, me.totalGames, if (didWin) 1.0 else 0.0)
        val newCurrentStreak = if (didWin) me.currentStreak + 1 else 0
        val newMaxStreak = maxOf(newCurrentStreak, me.maxStreak)
        val newTotalMovesMade = me.totalMovesMade + movesMade

        db.runTransaction { transaction ->
            val ref = db.collection("users").document(me.id)

            transaction.update(ref, mapOf(
                "totalGames" to me.totalGames + 1,
                "wonGames" to if (didWin) me.wonGames + 1 else me.wonGames,
                "lostGames" to if (didWin) me.lostGames else me.lostGames + 1,
                "rating" to newRating,
                "currentStreak" to newCurrentStreak,
                "maxStreak" to newMaxStreak,
                "totalMovesMade" to newTotalMovesMade

            ))

        }

    }

}