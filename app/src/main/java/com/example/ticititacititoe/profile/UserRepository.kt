package com.example.ticititacititoe.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = Firebase.firestore



    suspend fun searchUsers(searchTerm: String): List<User>{
        val snapshot = db.collection("users")
            .orderBy("username")
            .startAt(searchTerm)
            .endAt(searchTerm + "\uf8ff")
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
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


}