package com.example.ticititacititoe.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AuthViewModel: ViewModel() {
    // Speaking to AuthRepository to get auth info and sends to ui login,register,splash
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    fun isLoggedIn(): Boolean = auth.currentUser != null
}