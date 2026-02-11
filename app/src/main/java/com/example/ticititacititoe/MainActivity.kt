package com.example.ticititacititoe

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ticititacititoe.databinding.ActivityMainBinding
import com.example.ticititacititoe.game.ui.ChallengeFragment
import com.example.ticititacititoe.game.ui.GameActivity
import com.example.ticititacititoe.leaderboard.ui.LeaderboardActivity
import com.example.ticititacititoe.profile.ui.MyProfileActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.auth.ui.LoginActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var authViewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]



        binding.profileButton.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        }


        binding.highscoreButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        binding.newGameButton.setOnClickListener {
           val dialog = ChallengeFragment()
            dialog.show(supportFragmentManager, "challenge_fragment_dialog")
        }

    }

    fun newGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)

    }
}