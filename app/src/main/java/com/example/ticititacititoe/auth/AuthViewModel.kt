package com.example.ticititacititoe.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ticititacititoe.profile.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel: ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Unknown)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()


    private val auth = Firebase.auth
    private val repository = AuthRepository()
    fun isLoggedIn(): Boolean = repository.isLoggedIn()

    fun registerUser(username: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        _authUiState.value = AuthUiState.Unknown
        repository.registerUser(username, email, password) { result ->
            onResult(result)
            _authUiState.value = AuthUiState.LoggedIn
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        _authUiState.value = AuthUiState.Unknown
        repository.login(email, password, onSuccess, onFailure)
        _authUiState.value = AuthUiState.LoggedIn

    }

    fun logout(){
        repository.logout()
        _authUiState.value = AuthUiState.LoggedOut

    }
}