package com.example.ticititacititoe.profile.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.ActivityMyProfileBinding
import com.example.ticititacititoe.game.MultiplayerGameInvitationFragment
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.UserViewModel
import kotlinx.coroutines.launch
import kotlin.toString

class MyProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(this)[MultiplayerGameViewModel::class.java]


        val currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            multiplayerGameViewModel.startListeningForInvites(currentUserId)
            multiplayerGameViewModel.startListeningForOutgoingInvites(currentUserId)
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                multiplayerGameViewModel.incomingInvites.collect { invites ->
                    val existing = supportFragmentManager.findFragmentByTag("invite_dialog")

                    if (invites.isNotEmpty()) {
                        val invite = invites.first()

                        MultiplayerGameInvitationFragment
                            .newInstance(invite)
                            .show(supportFragmentManager, "invite_dialog")
                    } else {
                        if (existing is MultiplayerGameInvitationFragment) {
                            existing.dismissAllowingStateLoss()
                        }
                    }
                }


            }


        }


        binding.backButton.setOnClickListener {
            finish()
        }

        binding.settingsButton.setOnClickListener {
            val dialog = SettingsFragment()
            dialog.show(supportFragmentManager, "settings_fragment_dialog")
        }

        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.fetchCurrentUser()


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUser.collect { user ->
                    binding.usernameTextView.text = "${user?.username} ${user?.rating}"
                    binding.initialsTextView.text = user?.username?.take(2)
                    binding.wonGamesNumberTextView.text = user?.wonGames.toString()
                    binding.lostGamesNumberTextView.text = user?.lostGames.toString()
                    binding.totalGamesNumberTextView.text = user?.totalGames.toString()
                    binding.currentStreakTextView.text = getString(R.string.current_streak, user?.currentStreak)
                    binding.maxStreakTextView.text = getString(R.string.max_streak, user?.maxStreak)

                }
            }
        }

    }
}