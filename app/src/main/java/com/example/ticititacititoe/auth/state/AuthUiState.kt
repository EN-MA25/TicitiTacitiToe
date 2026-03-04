package com.example.ticititacititoe.auth.state

sealed interface AuthUiState {
    data object Loading: AuthUiState
    data object LoggedOut: AuthUiState
    data object LoggedIn: AuthUiState
}