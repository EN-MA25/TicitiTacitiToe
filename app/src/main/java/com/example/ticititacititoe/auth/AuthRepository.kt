package com.example.ticititacititoe.auth

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun register(
        username: String,
        email: String,
        password: String,
        onResult: (Task<AuthResult>) -> Unit
    ){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful) {
                    onResult(task)
                    return@addOnCompleteListener
                }

                val uid = task.result.user?.uid ?: return@addOnCompleteListener

                val user = hashMapOf(
                    "uid" to uid,
                    "username" to username,
                    "email" to email
                )

                Firebase.firestore
                    .collection("users")
                    .document(uid)
                    .set(user)
                    .addOnCompleteListener {
                        onResult(task)
                    }
            }
    }
}