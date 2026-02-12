package com.example.ticititacititoe.onlinegame

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnlineGameViewModel : ViewModel() {

    private val repository = OnlineGameRepository()

    private val _uiState = MutableLiveData(OnlineGameState())
    val uiState: LiveData<OnlineGameState> = _uiState
}