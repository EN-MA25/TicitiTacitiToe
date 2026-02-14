package com.example.ticititacititoe.onlinegame

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.databinding.ActivityGameBinding
import com.example.ticititacititoe.databinding.OnlineGameActivityBinding
import com.example.ticititacititoe.game.GameViewModel
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.google.firebase.auth.FirebaseAuth

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: OnlineGameActivityBinding
    private lateinit var gameId: String

    private val auth = FirebaseAuth.getInstance()
    private lateinit var onlineGameViewModel: OnlineGameViewModel

    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = OnlineGameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onlineGameViewModel = ViewModelProvider(this)[OnlineGameViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(this)[MultiplayerGameViewModel::class.java]

        val currentUserId = intent.getStringExtra("currentUserId")
        val fromUserId = intent.getStringExtra("fromUserId")

        //delete invitation
        multiplayerGameViewModel.deleteInvitations(currentUserId!!, fromUserId!!)

        //get game if exist
        onlineGameViewModel.getGameIfExist(currentUserId, fromUserId) {
            result ->
            if (result.isSuccess) {
                gameId = result.getOrNull()!!
            }
            else {
            //Create game state
            onlineGameViewModel.createOnlineGame(currentUserId, fromUserId, currentUserId) {
                result ->
                if (result.isSuccess) {
                    gameId = result.getOrNull()!!
                }
                else {
                    //handle error
                }
            }
        }
    }

        binding.onlinecell00.setOnClickListener {
            val (row, col) = binding.onlinecell00.tag.toString().split(",").map { it.toInt() }
            playerMakeMove(gameId, row, col)
        }
    }

    fun playerMakeMove(gameId: String?, row: Int, col: Int) {
        Log.d("!!!", "Pressed")
        onlineGameViewModel.playerMakeMove(gameId, row, col, auth.currentUser!!.uid) {

        }
    }
}