package com.example.ticititacititoe.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthViewModel: ViewModel() {

    private val auth = Firebase.auth
    private val repository = AuthRepository()
    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.login(email, password, onSuccess, onFailure)
    }

    fun logout(){
        repository.logout()
    }
}