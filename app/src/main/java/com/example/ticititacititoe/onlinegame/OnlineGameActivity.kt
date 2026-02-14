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

        //delete invitation
        multiplayerGameViewModel.deleteInvitations(currentUserId!!, fromUserId!!)

        //get game if exist
        onlineGameViewModel.getGameIfExist(currentUserId, fromUserId) {
            result ->
            if (result.isSuccess) {
                gameId = result.getOrNull()!!
                startListeningToMoves()
            }
            else {
            //Create game state
            onlineGameViewModel.createOnlineGame(currentUserId, fromUserId, currentUserId) {
                result ->
                if (result.isSuccess) {
                    gameId = result.getOrNull()!!
                    startListeningToMoves()
                }
                else {
                    //handle error
                }
            }
        }
    }

        binding.onlinecell00.setOnClickListener {
            val (row, col) = binding.onlinecell00.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell01.setOnClickListener {
            val (row, col) = binding.onlinecell01.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell02.setOnClickListener {
            val (row, col) = binding.onlinecell02.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell10.setOnClickListener {
            val (row, col) = binding.onlinecell10.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell11.setOnClickListener {
            val (row, col) = binding.onlinecell11.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell12.setOnClickListener {
            val (row, col) = binding.onlinecell12.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell20.setOnClickListener {
            val (row, col) = binding.onlinecell20.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell21.setOnClickListener {
            val (row, col) = binding.onlinecell21.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }

        binding.onlinecell22.setOnClickListener {
            val (row, col) = binding.onlinecell22.tag.toString().split(",").map { it.toLong() }
            playerMakeMove(gameId, row, col)
        }
    }

    fun startListeningToMoves() {
        onlineGameViewModel.startListenToMove(gameId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onlineGameViewModel.onlineState.collect { onlineState ->
                    renderBoard(onlineState)
                }
            }
        }
    }

    fun playerMakeMove(gameId: String?, row: Long, col: Long) {
        Log.d("!!!", "Pressed")
        onlineGameViewModel.playerMakeMove(gameId, row, col, auth.currentUser!!.uid) { result ->
            if (result.isSuccess) {
            }
            else {
                Toast.makeText(this, result.exceptionOrNull()?.message ?: "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun renderBoard(state: OnlineGameState) {
        val playerX = state.playerX
        for (move in state.moves) {
            if (move.player == playerX) {
                val imageButton = getButton(move.row.toInt(), move.col.toInt())
                imageButton.setImageResource(R.drawable.cell_x)
            } else {
                val imageButton = getButton(move.row.toInt(), move.col.toInt())
                imageButton.setImageResource(R.drawable.cell_o)
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