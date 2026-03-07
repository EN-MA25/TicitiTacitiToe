package com.example.ticititacititoe.user.ui

import android.content.Intent
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
import com.example.ticititacititoe.achievements.AchievementManager
import com.example.ticititacititoe.auth.state.AuthUiState
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.auth.ui.LoginActivity
import com.example.ticititacititoe.databinding.ActivityMyProfileBinding
import com.example.ticititacititoe.game.invitations.ui.IncomingInviteFragment
import com.example.ticititacititoe.game.invitations.InvitesViewModel
import com.example.ticititacititoe.user.UserViewModel
import kotlinx.coroutines.launch

class MyProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel

    private lateinit var invitesViewModel: InvitesViewModel

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
        invitesViewModel = ViewModelProvider(this)[InvitesViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        val currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            invitesViewModel.startListeningForInvites(currentUserId)
            invitesViewModel.startListeningForOutgoingInvites(currentUserId)
        }




        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authUiState.collect { state ->
                    if (state == AuthUiState.LoggedOut) {
                        val intent = Intent(this@MyProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                invitesViewModel.incomingInvites.collect { invites ->
                    val existing = supportFragmentManager.findFragmentByTag("invite_dialog")

                    if (invites.isNotEmpty()) {
                        val invite = invites.first()

                        IncomingInviteFragment
                            .newInstance(invite)
                            .show(supportFragmentManager, "invite_dialog")
                    } else {
                        if (existing is IncomingInviteFragment) {
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
                    user?.let {
                        binding.usernameTextView.text = "${user.username} ${user.rating}"
                        binding.initialsTextView.text = user.username?.take(2)
                        binding.currentStreakTextView.text =
                            getString(R.string.current_streak, user.currentStreak)
                        binding.maxStreakTextView.text =
                            getString(R.string.max_streak, user.maxStreak)
                        binding.wonGamesNumberTextView.text = user.wonGames.toString()
                        binding.lostGamesNumberTextView.text = user.lostGames.toString()
                        binding.totalGamesNumberTextView.text = user.totalGames.toString()

                        binding.achievementsButton.text = "Achievements ${user.achievements.size}/${AchievementManager.achievements.size}"

                        binding.achievementsButton.setOnClickListener {

                            val fragment = AchievementsFragment().apply {
                                arguments = Bundle().apply {
                                    putString(USER_ID, user.id)
                                }
                            }
                            fragment.show(supportFragmentManager, "achievement_fragment_dialog")
                        }

                    }

                }
            }
        }
    }
}