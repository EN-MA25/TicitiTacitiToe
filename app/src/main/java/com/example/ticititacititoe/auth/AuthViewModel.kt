package com.example.ticititacititoe.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch


class AuthViewModel: ViewModel(

) {


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

    fun loginWithGoogle(context: Context, credentialManager: CredentialManager) {
        viewModelScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val authResult = repository.handleSignIn(result)



            } catch (exception: GetCredentialException) {
                repository.handleFailure(exception, context)
            }
        }
    }


}