package com.example.ticititacititoe.onlinegame

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.databinding.ActivityGameBinding
import com.example.ticititacititoe.game.GameViewModel
import com.google.firebase.auth.FirebaseAuth

class OnlineGameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameBinding

    private val auth = FirebaseAuth.getInstance()
    private lateinit var OnlineGameViewModel: OnlineGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        OnlineGameViewModel = ViewModelProvider(this)[OnlineGameViewModel::class.java]
    }

    fun squareIsPressed(view: View) {
        Log.d("!!!", "Pressed")
        val (row, col) = view.tag.toString().split(",").map { it.toInt() }
        OnlineGameViewModel.playerMakeMove(row, col, auth.currentUser!!.uid)
    }
}