package com.example.ticititacititoe.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticititacititoe.profile.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel: ViewModel() {



    private val auth = Firebase.auth
    private val repository = AuthRepository()
    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun registerUser(username: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        repository.registerUser(username, email, password) { result ->
            onResult(result)
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.login(email, password, onSuccess, onFailure)
    }

    fun logout(){
        repository.logout()
    }
}