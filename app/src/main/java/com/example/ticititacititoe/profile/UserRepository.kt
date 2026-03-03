package com.example.ticititacititoe.profile

import com.example.ticititacititoe.Util
import com.example.ticititacititoe.achievements.AchievementManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

    suspend fun updateUserAfterGame(
        me: User,
        opponent: User,
        didWin: Boolean,
        movesMade: Int
    ): List<String> {

        val newRating = Util.newRating(
            me.rating,
            opponent.rating,
            me.totalGames,
            if (didWin) 1.0 else 0.0
        )

        val newCurrentStreak = if (didWin) me.currentStreak + 1 else 0
        val newMaxStreak = maxOf(newCurrentStreak, me.maxStreak)
        val newTotalMovesMade = me.totalMovesMade + movesMade

        val updatedUser = me.copy(
            totalGames = me.totalGames + 1,
            wonGames = if (didWin) me.wonGames + 1 else me.wonGames,
            lostGames = if (didWin) me.lostGames else me.lostGames + 1,
            rating = newRating,
            currentStreak = newCurrentStreak,
            maxStreak = newMaxStreak,
            totalMovesMade = newTotalMovesMade
        )

        val newAchievements =
            AchievementManager.checkForNewAchievements(updatedUser)

        val ref = db.collection("users").document(me.id)

        val now = FieldValue.serverTimestamp()
        db.runTransaction { transaction ->

            val updates = mutableMapOf<String, Any>(
                "totalGames" to updatedUser.totalGames,
                "wonGames" to updatedUser.wonGames,
                "lostGames" to updatedUser.lostGames,
                "rating" to updatedUser.rating,
                "currentStreak" to updatedUser.currentStreak,
                "maxStreak" to updatedUser.maxStreak,
                "totalMovesMade" to updatedUser.totalMovesMade,
                "lastPlayedGame" to now
            )

            if (didWin) {
                updates["lastWonGame"] = now
            } else {
                updates["lastLostGame"] = now
            }

            for (id in newAchievements) {
                updates["achievements.$id"] =
                    now
            }

            transaction.update(ref, updates)

        }.await()

        return newAchievements
    }

}