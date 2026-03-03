package com.example.ticititacititoe.onlinegame

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.MainActivity
import com.example.ticititacititoe.R
import com.example.ticititacititoe.achievements.AchievementManager
import com.example.ticititacititoe.chat.ChatFragment
import com.example.ticititacititoe.chat.ChatViewModel
import com.example.ticititacititoe.databinding.OnlineGameActivityBinding
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.User
import com.example.ticititacititoe.profile.UserViewModel
import kotlinx.coroutines.launch


class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: OnlineGameActivityBinding
    private lateinit var gameId: String
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
    private var playerX: String? = ""
    private var playerO: String? = ""


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

                val myUid = userViewModel.getCurrentUserId()
                Toast.makeText(this, if (myUid == currentUserId) "You are Player X" else "You are Player O", Toast.LENGTH_LONG).show()

                openChatFragment()
            } else {
                playerX = currentUserId
                playerO = otherUserId

                lifecycleScope.launch {
                    // ========== Create game state ==========
                   val result = onlineGameViewModel.createOnlineGame(
                        playerX,
                        playerO,
                        playerX
                    )
                   result.onSuccess { newGameId ->
                       gameId = newGameId!!
                       startListeningToMoves()
                       chatViewModel.createChatRoom(gameId, userIds)

                       openChatFragment()

                       val myUid = userViewModel.getCurrentUserId()
                       Toast.makeText(this@OnlineGameActivity,  if (myUid == currentUserId) "You are Player X" else "You are Player O", Toast.LENGTH_LONG).show()
                   }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.achievementEvents.collect { achievements ->
                    showAchievementsPopup(achievements)
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            chatViewModel.deleteChat(gameId)
            onlineGameViewModel.userHasLeft(gameId, userViewModel.getCurrentUserId())

            finish()
        }
    }

    private fun showAchievementsPopup(ids: List<String>) {

        val achievements = AchievementManager.achievements.filter { (id, title, description, condition) ->
            ids.contains(id)
        }

        for (achievement in achievements) {
            AlertDialog.Builder(this)
                .setTitle(achievement.title)
                .setMessage(achievement.description)
                .setPositiveButton("Nice!") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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
                    onlineGameViewModel.addOnlineGameResult(gameId,gameResult, System.currentTimeMillis(), onlineGameState.moves.size) { result ->
                        if (result.isSuccess) {
                            Toast.makeText(
                                this,
                                "Other player has left, you won!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onlineGameViewModel.deleteGame(gameId) {
                                chatViewModel.deleteChat(gameId)
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
        val gameLogic = OnlineGameLogic()

        // ========== Collect online state via stateflow and render board ==========
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onlineGameViewModel.onlineState.collect { onlineState ->
                    renderBoard(onlineState)
                    renderStatus(onlineState)

                    val isWinner = gameLogic.checkWinner(onlineState)
                    if (isWinner) {
                        val winnerUid = if (onlineState.gameResult.contains("X")) onlineState.playerX!! else onlineState.playerO!!
                        val loserUid = if (winnerUid == onlineState.playerX) onlineState.playerO!! else onlineState.playerX!!
                        val resultToAdd = OnlineGameResult(
                            playerWhoWon = winnerUid,
                            playerWhoLost = loserUid,
                            movesMade =  onlineState.moves.size
                        )

                        val timestamp = System.currentTimeMillis()
                        val currentUserId = userViewModel.getCurrentUserId()

                        if (currentUserId == winnerUid) {
                            onlineGameViewModel.addOnlineGameResult(gameId, resultToAdd, timestamp, onlineState.moves.size) {
                                onlineGameViewModel.deleteGame(gameId) {}
                            }
                        }
                            //lifecycleScope.launch {
                              //  kotlinx.coroutines.delay(500)
                                onlineGameViewModel.fetchGameResult(gameId)
                           // }




                        userViewModel.updateUserAfterGame(me!!, opponent!!, (winnerUid == me!!.id), movesMade)


                    }



                    isGameOver(onlineState)
                }
            }
        }

    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED){
            onlineGameViewModel.gameResult.collect { result ->
                result?.let {
                    val dialog = GameOverFragment(playerX)
                    dialog.isCancelable = false
                    dialog.show(supportFragmentManager, "game_over_dialog")
                    }
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