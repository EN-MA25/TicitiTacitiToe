package com.example.ticititacititoe.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.R
import com.example.ticititacititoe.auth.state.AuthUiState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption

import kotlinx.coroutines.launch

class AuthViewModel: ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()



    private val repository = AuthRepository()
    fun isLoggedIn(): Boolean = repository.isLoggedIn()


    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        _authUiState.value = if (repository.isLoggedIn()) {
            AuthUiState.LoggedIn
        } else {
            AuthUiState.LoggedOut
        }
    }
    fun registerUser(username: String, email: String, password: String, onResult: (Result<Unit>) -> Unit) {
        _authUiState.value = AuthUiState.Loading
        repository.registerUser(username, email, password) { result ->
            onResult(result)
            if(result.isSuccess) {
                _authUiState.value = AuthUiState.LoggedIn

            } else {
                _authUiState.value = AuthUiState.LoggedOut
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        _authUiState.value = AuthUiState.Loading
        repository.login(email, password, onSuccess = {
            _authUiState.value = AuthUiState.LoggedIn

        }, onFailure = {
            _authUiState.value = AuthUiState.LoggedOut

        })

    }

    fun logout(){
        repository.logout()
        _authUiState.value = AuthUiState.LoggedOut

    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
    repository.resetPassword(email,onSuccess, onFailure)
    }

    fun loginWithGoogle(context: Context, credentialManager: CredentialManager) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading

            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val signInResult = repository.handleSignIn(result)

                if (signInResult.isSuccess) {
                    _authUiState.value = AuthUiState.LoggedIn

                } else {
                    _authUiState.value = AuthUiState.LoggedOut

                }



            } catch (exception: GetCredentialException) {
                repository.handleFailure(exception, context)
                _authUiState.value = AuthUiState.LoggedOut
            }
        }
    }


}