package com.example.ticititacititoe.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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


}