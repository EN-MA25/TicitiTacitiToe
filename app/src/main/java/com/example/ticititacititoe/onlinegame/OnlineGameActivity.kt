package com.example.ticititacititoe.onlinegame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.MainActivity
import com.example.ticititacititoe.R
import com.example.ticititacititoe.chat.ChatFragment
import com.example.ticititacititoe.chat.ChatViewModel
import com.example.ticititacititoe.databinding.OnlineGameActivityBinding
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.User
import com.example.ticititacititoe.profile.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.view.Gravity

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: OnlineGameActivityBinding
    private lateinit var gameId: String
    private var hasShownPlayerRole = false

    private val auth = FirebaseAuth.getInstance()
    private lateinit var onlineGameViewModel: OnlineGameViewModel

    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel
    private lateinit var chatViewModel: ChatViewModel

    private lateinit var userViewModel: UserViewModel

    private var hasStartedListening = false

    private var me: User? = null
    private var opponent: User? = null

    private var currentUserId: String? = ""
    private var otherUserId: String? = ""
    private var movesMade = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = OnlineGameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(0, 0, 0, imeInsets.bottom)

            insets
        }

        binding.onlineNewGameButton.visibility = View.GONE
        onlineGameViewModel = ViewModelProvider(this)[OnlineGameViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(this)[MultiplayerGameViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

        binding.onlineNewGameButton.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()

        currentUserId = intent.getStringExtra("currentUserId")
        otherUserId = intent.getStringExtra("fromUserId")

        val meId = userViewModel.getCurrentUserId()

        if (meId!! == currentUserId!!) {
            userViewModel.getUserDetailsById(currentUserId) { user -> me = user }
            userViewModel.getUserDetailsById(otherUserId) { user -> opponent = user }
        } else {
            userViewModel.getUserDetailsById(currentUserId) { user -> opponent = user }
            userViewModel.getUserDetailsById(otherUserId) { user -> me = user }
        }

        val userIds = mutableListOf(currentUserId, otherUserId)

        // ========== Delete invitaions from db ==========
        multiplayerGameViewModel.deleteInvitations(currentUserId!!, otherUserId!!)
        // ========== Get game if exist ==========
        onlineGameViewModel.getGameIfExist(currentUserId, otherUserId) { result ->
            if (result.isSuccess) {
                gameId = result.getOrNull()!!
                startListeningToMoves()
                chatViewModel.createChatRoom(gameId, userIds)

                openChatFragment()
            } else {
                // ========== Create game state ==========
                onlineGameViewModel.createOnlineGame(
                    currentUserId,
                    otherUserId,
                    currentUserId
                ) { result ->
                    if (result.isSuccess) {
                        gameId = result.getOrNull()!!
                        startListeningToMoves()
                        chatViewModel.createChatRoom(gameId, userIds)

                        openChatFragment()
                    } else {
                        //Toast.makeText(this, "Could not create game", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun squarePressed(view: View) {
        val (row, col) = view.tag.toString().split(",").map { it.toLong() }
        playerMakeMove(gameId, row, col)
    }

    fun isGameOver(onlineGameState: OnlineGameState) {
        if (onlineGameState.playerLeftId != "") {
            if (onlineGameState.playerLeftId != userViewModel.getCurrentUserId()) {
                if (onlineGameState.gameResult == "Ongoing") {
                    val gameResult = OnlineGameResult(otherUserId, currentUserId)
                    onlineGameViewModel.addOnlineGameResult(gameResult, System.currentTimeMillis(), onlineGameState.moves.size) { result ->
                        if (result.isSuccess) {
                            Toast.makeText(
                                this,
                                "Other player has left, you won!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onlineGameViewModel.deleteGame(gameId) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }

    fun startListeningToMoves() {
        if (hasStartedListening)
            return

        onlineGameViewModel.startListenToMove(gameId)
        hasStartedListening = true

        // ========== Collect online state via stateflow and render board ==========
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onlineGameViewModel.onlineState.collect { onlineState ->
                    renderBoard(onlineState)
                    renderStatus(onlineState)
                    if (checkWinner(onlineState)) {
                        val winner: String
                        if (onlineState.gameResult.contains("X")) {
                            winner = "Player X"
                        } else {
                            winner = "Player O"
                        }
                        val dialog = GameOverFragment(winner)
                        dialog.isCancelable = false
                        dialog.show(supportFragmentManager, "game_over_dialog")

//                        Toast.makeText(
//                            this@OnlineGameActivity,
//                            onlineState.gameResult,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    isGameOver(onlineState)
                }
            }
        }
    }

    fun playerMakeMove(gameId: String?, row: Long, col: Long) {

        // ========== Call viewmodel and send gameid, row/col, uid ==========
        onlineGameViewModel.playerMakeMove(
            gameId,
            row,
            col,
            userViewModel.getCurrentUserId()
        ) { result ->
            if (result.isSuccess) {
                movesMade++
            } else {
                Toast.makeText(
                    this,
                    result.exceptionOrNull()?.message ?: "Something went wrong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderStatus(state: OnlineGameState) {

        val myUid = FirebaseAuth.getInstance().currentUser?.uid

        if (!hasShownPlayerRole &&
            myUid != null && !state.playerX.isNullOrEmpty() && !state.playerO.isNullOrEmpty()) {
            Toast.makeText(this, if (myUid == state.playerX) "You are Player X" else "You are Player O", Toast.LENGTH_LONG).show()

            hasShownPlayerRole = true
        }

        if (state.gameResult != "Ongoing")
            return

        if (state.currentPlayerUid == state.playerX) {

            binding.onlineStatusTextView.text = "X"
            binding.onlineStatusTextView.setBackgroundResource(R.drawable.speech_bubble_red_border_red_center)

        } else {
            binding.onlineStatusTextView.text = "O"
            binding.onlineStatusTextView.setBackgroundResource(R.drawable.speech_bubble_red_border_blue_center)
        }
    }

    private fun checkWinner(state: OnlineGameState): Boolean {

        //Get player from state
        val playerX = state.playerX

        // Lists for hold every move
        val playerXMoves = mutableListOf<OnlineMove>()
        val playerOMoves = mutableListOf<OnlineMove>()

        // Loop through every move in game
        for (move in state.moves.takeLast(6)) {
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
            if (combinations.all { (r, c) -> playerXMoves.any { it.row.toInt() == r && it.col.toInt() == c && state.gameResult == "Ongoing" } }) {

                state.gameResult = "Player X won"
                onlineGameViewModel.updateGameResult(state.gameId, "Player X won")

                val gameResult =
                    OnlineGameResult(playerWhoWon = playerX, playerWhoLost = state.playerO)
                val currentUserId = userViewModel.getCurrentUserId()
                if (gameResult._playerWhoWon == currentUserId) {
                    onlineGameViewModel.addOnlineGameResult(gameResult, System.currentTimeMillis(),state.moves.size) { result ->
                        if (result.isSuccess) {
                            onlineGameViewModel.deleteGame(gameId) {
                            }
                        }
                    }
                    userViewModel.updateUserAfterGame(me!!, opponent!!, true, movesMade)
                } else {
                    userViewModel.updateUserAfterGame(me!!, opponent!!, false, movesMade)
                }
                return true
            }
        }

        // Check if playerO has any win combo
        for (combo in winningPositions) {
            if (combo.all { (r, c) -> playerOMoves.any { it.row.toInt() == r && it.col.toInt() == c && state.gameResult == "Ongoing" } }) {

                state.gameResult = "Player O won"
                onlineGameViewModel.updateGameResult(state.gameId, "Player O won")

                val gameResult =
                    OnlineGameResult(playerWhoWon = state.playerO, playerWhoLost = state.playerX)
                if (gameResult._playerWhoWon == userViewModel.getCurrentUserId()) {
                    onlineGameViewModel.addOnlineGameResult(gameResult, System.currentTimeMillis(), state.moves.size) { result ->
                        if (result.isSuccess) {
                            onlineGameViewModel.deleteGame(gameId) {

                            }
                        }
                    }
                    userViewModel.updateUserAfterGame(me!!, opponent!!, true, movesMade)
                } else {
                    userViewModel.updateUserAfterGame(me!!, opponent!!, false, movesMade)
                }
                return true
            }
        }
        return false
    }

    private fun clearBoard() {

        for (r in 0..2) {
            for (c in 0..2) {
                val imageButton = getButton(r, c)
                imageButton.setImageDrawable(null)
                imageButton.alpha = 1.0f

            }
        }
    }

    // ========== Update board ==========
    private fun renderBoard(state: OnlineGameState) {

        clearBoard()

        val playerX = state.playerX
        for ((index, move) in state.moves.takeLast(6).withIndex()) {


            // ========== Control who made the move and show right imagebutton ==========
            val imageButton = getButton(move.row.toInt(), move.col.toInt())

            if (move.player!! == playerX!!) {
                imageButton.setImageResource(R.drawable.cell_x)
            } else {
                imageButton.setImageResource(R.drawable.cell_o)
            }
            if (index == 0 && state.moves.size >= 6) {
                imageButton.alpha = 0.5f
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        onlineGameViewModel.getGameIfExist(currentUserId, otherUserId) { result ->
            if (result.isSuccess) {
                onlineGameViewModel.userHasLeft(gameId, userViewModel.getCurrentUserId())
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

    private fun openChatFragment(){
        val chatFragment = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("gameId", gameId)
                putString("opponentId", otherUserId)
                putString("opponentUsername", opponent?.username ?: "")
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.chatContainer.id, chatFragment)
            .commit()
    }
}