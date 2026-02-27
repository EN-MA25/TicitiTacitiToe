package com.example.ticititacititoe.auth

sealed interface AuthUiState {
    data object Loading: AuthUiState
    data object LoggedOut: AuthUiState
    data object LoggedIn: AuthUiState
}