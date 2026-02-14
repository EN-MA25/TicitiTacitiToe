package com.example.ticititacititoe.onlinegame

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.ActivityGameBinding
import com.example.ticititacititoe.databinding.OnlineGameActivityBinding
import com.example.ticititacititoe.game.GameResult
import com.example.ticititacititoe.game.GameState
import com.example.ticititacititoe.game.GameViewModel
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.example.ticititacititoe.game.Player
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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

        setupObservers()

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

        binding.onlinecell01.setOnClickListener {
            val (row, col) = binding.onlinecell01.tag.toString().split(",").map { it.toInt() }
            playerMakeMove(gameId, row, col)
        }
    }

    fun playerMakeMove(gameId: String?, row: Int, col: Int) {
        Log.d("!!!", "Pressed")
        onlineGameViewModel.playerMakeMove(gameId, row, col, auth.currentUser!!.uid) { result ->
            if (result.isSuccess) {
                //render move
                renderBoard(onlineGameViewModel.uiState.value)
            }
            else {
                Toast.makeText(this, result.getOrDefault(""), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onlineGameViewModel.uiState.collect { state ->
                    renderBoard(state)
//                   renderStatus(state)
                }
            }
        }
    }

    private fun renderBoard(state: OnlineGameState) {
        for (r in 0..2) {
            for (c in 0..2) {
                val player = state.board[r][c]
                val imageButton = getButton(r, c)

                when (player) {
                    Player.X -> imageButton.setImageResource(R.drawable.cell_x)
                    Player.O -> imageButton.setImageResource(R.drawable.cell_o)
                    null -> imageButton.setImageDrawable(null)
                }

//                imageButton.alpha = if (state.oldestMove == (r to c)) 0.5f else 1.0f

            }
        }
    }

    private fun getButton(row: Int, col: Int): ImageButton {
        return when (row to col) {
            0 to 0 -> binding.onlinecell00
            0 to 1 -> binding.onlinecell01
            0 to 2 -> binding.onlinecell02
            1 to 0 -> binding.onlinecell10
            1 to 1 -> binding.onlinecell11
            1 to 2 -> binding.onlinecell12
            2 to 0 -> binding.onlinecell20
            2 to 1 -> binding.onlinecell21
            2 to 2 -> binding.onlinecell22
            else -> error("Invalid cell")
        }
    }
}