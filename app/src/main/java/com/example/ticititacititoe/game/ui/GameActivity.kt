package com.example.ticititacititoe.game.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.ActivityGameBinding
import com.example.ticititacititoe.game.model.GameResult
import com.example.ticititacititoe.game.state.GameState
import com.example.ticititacititoe.game.GameViewModel
import com.example.ticititacititoe.game.model.Player
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    private lateinit var gameViewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        gameViewModel = ViewModelProvider(this)[GameViewModel::class.java]

        binding.newGameButton.visibility = View.INVISIBLE
        setupObservers()

        binding.backButton?.setOnClickListener {
            finish()
        }

    }

    fun squarePressed(view: View) {
        Log.d("!!!", "Pressed")
        val (row, col) = view.tag.toString().split(",").map { it.toInt() }
        gameViewModel.makeMove(row, col)
    }

    fun playAgainPressed(view: View) {
        gameViewModel.resetGame()

        val params = binding.statusTextView.layoutParams
        params.height = resources.getDimensionPixelSize(R.dimen.turn_pic_ordinary_size)
        params.width = resources.getDimensionPixelSize(R.dimen.turn_pic_ordinary_size)
        binding.statusTextView.layoutParams = params

        view.visibility = View.INVISIBLE
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameViewModel.uiState.collect { state ->
                    renderBoard(state)
                    renderStatus(state)
                }
            }
        }
    }


    private fun renderBoard(state: GameState) {
        for (r in 0..2) {
            for (c in 0..2) {
                val player = state.board[r][c]
                val imageButton = getButton(r, c)

                when (player) {
                    Player.X -> imageButton.setImageResource(R.drawable.cell_x)
                    Player.O -> imageButton.setImageResource(R.drawable.cell_o)
                    null -> imageButton.setImageDrawable(null)
                }

                imageButton.alpha = if (state.oldestMove == (r to c)) 0.5f else 1.0f

            }
        }
    }

    private fun renderStatus(state: GameState) {
        when (state.gameResult) {
            is GameResult.Ongoing -> {
                binding.statusTextView.text = getString(R.string.turn, state.currentPlayer)

                when (state.currentPlayer) {
                    Player.X -> {
                        binding.statusTextView.setBackgroundResource(R.drawable.speech_bubble_red_border_red_center)
                    }
                    Player.O -> {
                        binding.statusTextView.setBackgroundResource(R.drawable.speech_bubble_red_border_blue_center)
                    }
                }
            }
            is GameResult.Win -> {
                binding.statusTextView.text = getString(R.string.winner, state.gameResult.winner)
                val params = binding.statusTextView.layoutParams
                params.height = resources.getDimensionPixelSize(R.dimen.turn_pic_big_size)
                params.width = resources.getDimensionPixelSize(R.dimen.turn_pic_big_size)
                binding.statusTextView.layoutParams = params

                binding.newGameButton.visibility = View.VISIBLE

            }
            is GameResult.Draw -> {
                binding.statusTextView.text = getString(R.string.draw)
            }
        }
    }

    private fun getButton(row: Int, col: Int): ImageButton {
        return when (row to col) {
            0 to 0 -> binding.cell00
            0 to 1 -> binding.cell01
            0 to 2 -> binding.cell02
            1 to 0 -> binding.cell10
            1 to 1 -> binding.cell11
            1 to 2 -> binding.cell12
            2 to 0 -> binding.cell20
            2 to 1 -> binding.cell21
            2 to 2 -> binding.cell22
            else -> error("Invalid cell")
        }
    }

}