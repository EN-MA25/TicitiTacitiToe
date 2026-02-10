package com.example.ticititacititoe.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModel: ViewModel() {
    private val repository = AuthRepository()
    
    fun registerUser(username: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        repository.registerUser(username, email, password) { result ->
            onResult(result)
        }
    }
}