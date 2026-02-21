package com.example.ticititacititoe.onlinegame

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
import androidx.transition.Visibility
import com.example.ticititacititoe.R
import com.example.ticititacititoe.chat.ChatFragment
import com.example.ticititacititoe.chat.ChatViewModel
import com.example.ticititacititoe.databinding.OnlineGameActivityBinding
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.UserViewModel
import com.example.ticititacititoe.profile.ui.SearchUserFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: OnlineGameActivityBinding
    private lateinit var gameId: String

    private val auth = FirebaseAuth.getInstance()
    private lateinit var onlineGameViewModel: OnlineGameViewModel

    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var opponentUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = OnlineGameActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Vi sätter padding på fragmentets rot så att EditText trycks upp
            // Vi tar imeInsets.bottom (tangentbordet) men drar bort systemBars.bottom
            // för att inte få dubbel padding om navigationsfältet redan finns där.
            v.setPadding(0, 0, 0, imeInsets.bottom)

            insets
        }

        binding.onlineNewGameButton.visibility = View.GONE
        onlineGameViewModel = ViewModelProvider(this)[OnlineGameViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(this)[MultiplayerGameViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]


        val currentUserId = intent.getStringExtra("currentUserId")
        val fromUserId = intent.getStringExtra("fromUserId")



        if (fromUserId != null) {
            userViewModel.loadUserById(fromUserId)
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.selectedUser.collect { user ->
                    opponentUsername = user?.username ?: return@collect


                }
            }
        }

        //delete invitation
        multiplayerGameViewModel.deleteInvitations(currentUserId!!, fromUserId!!)
        val userIds = listOf(currentUserId, fromUserId)

        //get game if exist
        onlineGameViewModel.getGameIfExist(currentUserId, fromUserId) {
            result ->
            if (result.isSuccess) {
                gameId = result.getOrNull()!!
                startListeningToMoves()
                chatViewModel.createChatRoom(gameId, userIds)

                val chatFragment = ChatFragment().apply {
                    arguments = Bundle().apply {
                        putString("gameId", gameId)
                        putString("opponentId", fromUserId)
                        putString("opponentUsername", opponentUsername)
                    }
                }

                supportFragmentManager.beginTransaction()
                    .replace(binding.chatContainer.id, chatFragment)
                    .commit()

            }
            else {
            //Create game state
            onlineGameViewModel.createOnlineGame(currentUserId, fromUserId, currentUserId) {
                result ->
                if (result.isSuccess) {
                    gameId = result.getOrNull()!!
                    startListeningToMoves()
                    chatViewModel.createChatRoom(gameId, userIds)

                }
                else {
                    //handle error
                }
            }
        }
    }


//        binding.chatButton.setOnClickListener {
//            val chatFragment = ChatFragment().apply {
//                arguments = Bundle().apply {
//                    putString("gameId", gameId)
//                    putString("opponentId", fromUserId)
//                    putString("opponentUsername", opponentUsername )
//
//                }
//            }
//            chatFragment.show(supportFragmentManager, "chat_fragment_dialog")
//        }
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

        binding.onlineNewGameButton.visibility = View.GONE
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