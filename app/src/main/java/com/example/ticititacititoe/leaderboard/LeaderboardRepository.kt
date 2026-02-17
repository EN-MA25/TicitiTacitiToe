package com.example.ticititacititoe.leaderboard

import com.example.ticititacititoe.profile.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class LeaderboardRepository {
    private val db = Firebase.firestore

    suspend fun getAllUsers(): List<User>{
        val snapshot = db.collection("users")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }

    }


     fun getRatings(userId: String, callback: (Double?) -> Unit){
        val docRef = db.collection("users")
                          .document(userId)
                           docRef.get()
                               .addOnSuccessListener{document ->
                                   if(document != null && document.exists()){
                                     val rating = document.getDouble("raiting")
                                       callback(rating)
                                   }else{
                                       callback(null)
                                   }
                               }
                               .addOnFailureListener {
                                   callback(null)
                               }




    }
}