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
import com.example.ticititacititoe.profile.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: OnlineGameActivityBinding
    private lateinit var gameId: String

    private lateinit var onlineGameViewModel: OnlineGameViewModel

    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel

    private lateinit var userViewModel: UserViewModel

    private var hasStartedListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = OnlineGameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onlineGameViewModel = ViewModelProvider(this)[OnlineGameViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(this)[MultiplayerGameViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // ========== Cell click listeners ==========
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

    override fun onStart() {
        super.onStart()

        val currentUserId = intent.getStringExtra("currentUserId")
        val fromUserId = intent.getStringExtra("fromUserId")

        // ========== Delete invitaions from db ==========
        multiplayerGameViewModel.deleteInvitations(currentUserId!!, fromUserId!!)
        // ========== Get game if exist ==========
        onlineGameViewModel.getGameIfExist(currentUserId, fromUserId) {
                result ->
            if (result.isSuccess) {
                gameId = result.getOrNull()!!
                startListeningToMoves()
            }
            else {
                // ========== Create game state ==========
                onlineGameViewModel.createOnlineGame(currentUserId, fromUserId, currentUserId) {
                        result ->
                    if (result.isSuccess) {
                        gameId = result.getOrNull()!!
                        startListeningToMoves()
                    }
                    else {
                        //Toast.makeText(this, "Could not create game", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun startListeningToMoves() {
        if (hasStartedListening) return

        onlineGameViewModel.startListenToMove(gameId)
        hasStartedListening = true

        // ========== Collect online state via stateflow and render board ==========

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onlineGameViewModel.onlineState.collect { onlineState ->
                    renderBoard(onlineState)
                    val currentUser = userViewModel.getCurrentUserId()
                    Log.d("!!!", "CURRENTUSER: " + currentUser)
                    if (checkWinner(onlineState)){
                        Toast.makeText(this@OnlineGameActivity, onlineState.gameResult , Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun playerMakeMove(gameId: String?, row: Long, col: Long) {

        // ========== Call viewmodel and send gameid, row/col, uid ==========
        onlineGameViewModel.playerMakeMove(gameId, row, col, userViewModel.getCurrentUserId()) { result ->
            if (result.isSuccess) {
            // Updates UI
            }
            else {
                Toast.makeText(this, result.exceptionOrNull()?.message ?: "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkWinner(state: OnlineGameState): Boolean {

        //Get player from state
        val playerX = state.playerX

        // Lists for hold every move
        val playerXMoves = mutableListOf<OnlineMove>()
        val playerOMoves = mutableListOf<OnlineMove>()

        // Loop through every move in game
        for (move in state.moves) {
            // Add moves in playerWMoves if player is playerX
            if (playerX == move.player) {
                playerXMoves.add(move)
            } else {
                playerOMoves.add(move)
            }
        }

            // List of winning combos
            val winningPositions = listOf(
                listOf(0 to 0, 0 to 1, 0 to 2),
                listOf(1 to 0, 1 to 1, 1 to 2),
                listOf(2 to 0, 2 to 1, 2 to 2),
                listOf(0 to 0, 1 to 0, 2 to 0),
                listOf(0 to 1, 1 to 1, 2 to 1),
                listOf(0 to 2, 1 to 2, 2 to 2),
                listOf(0 to 0, 1 to 1, 2 to 2),
                listOf(0 to 2, 1 to 1, 2 to 0)
            )
            // Check if playerX has any win combo
            for (combinations in winningPositions) {
                if (combinations.all {(r, c) -> playerXMoves.any { it.row.toInt() == r && it.col.toInt() == c && state.gameResult == "Ongoing" } }) {

                state.gameResult = "Player X won"
                    onlineGameViewModel.updateGameResult(state.gameId, "Player X won")


                val gameResult = OnlineGameResult(playerWhoWon = playerX, playerWhoLost = state.playerO)
                    val currentUserId = userViewModel.getCurrentUserId()
                    if (gameResult._playerWhoWon == currentUserId) {
                    onlineGameViewModel.addOnlineGameResult(gameResult) { result ->
                        if (result.isSuccess) {

                        }
                    }
                        return true
                }

                }
            }
            // Check if playerO has any win combo
            for (combo in winningPositions) {
                if (combo.all { (r, c) -> playerOMoves.any { it.row.toInt() == r && it.col.toInt() == c && state.gameResult == "Ongoing" } }) {

                    state.gameResult = "Player O won"
                    onlineGameViewModel.updateGameResult(state.gameId, "Player O won")

                    val gameResult = OnlineGameResult(playerWhoWon = state.playerO, playerWhoLost = state.playerX)
                    if (gameResult._playerWhoWon == userViewModel.getCurrentUserId()) {
                        onlineGameViewModel.addOnlineGameResult(gameResult) { result ->
                            if (result.isSuccess) {

                            }
                        }
                        return true
                    }
                }
            }
    return false
    }

    // ========== Update board ==========
    private fun renderBoard(state: OnlineGameState) {
        val playerX = state.playerX
        for (move in state.moves) {

            // ========== Control who made the move and show right imagebutton ==========
            if (move.player!! == playerX!!) {
                val imageButton = getButton(move.row.toInt(), move.col.toInt())
                imageButton.setImageResource(R.drawable.cell_x)
            } else {
                val imageButton = getButton(move.row.toInt(), move.col.toInt())
                imageButton.setImageResource(R.drawable.cell_o)
            }
        }
    }

    private fun getButton(row: Int, col: Int): ImageButton {
        // ========== Return correct imagebutton based on row/col ==========
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