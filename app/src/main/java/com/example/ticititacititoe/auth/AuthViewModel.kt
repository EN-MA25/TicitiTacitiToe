package com.example.ticititacititoe.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

class AuthViewModel: ViewModel() {
    // Speaking to AuthRepository to get auth info and sends to ui login,register,splash
    private val repository = AuthRepository()

    private val _registerResult = MutableLiveData<Result<String>>()
    val registerResult: LiveData<Result<String>> = _registerResult

    fun registerUser(username: String, email: String, password: String, onResult: (Task<AuthResult>) -> Unit){
        repository.register(username, email, password){
          task -> onResult(task)
        }
    }
}