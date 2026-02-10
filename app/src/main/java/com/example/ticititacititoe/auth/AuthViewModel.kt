package com.example.ticititacititoe.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AuthViewModel: ViewModel() {
    private val repository = AuthRepository()

    // =========== LiveData for registration result ============
    private val _registerResult = MutableLiveData<Result<Unit>>()
    val registerResult: LiveData<Result<Unit>> = _registerResult

    fun registerUser(username: String, email: String, password: String,) {
        repository.registerUser(username, email, password) { result ->
            _registerResult.postValue(result)
        }
    }
}