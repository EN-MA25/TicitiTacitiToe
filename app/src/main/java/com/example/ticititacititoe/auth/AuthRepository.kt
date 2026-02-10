package com.example.ticititacititoe.auth

import com.google.firebase.auth.FirebaseAuth

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess()}
            .addOnFailureListener { onFailure(it)}
    }

    fun logout(){
        auth.signOut()
    }
}