package com.example.ticititacititoe.leaderboard.ui

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
import com.example.ticititacititoe.databinding.ActivityLeaderboardBinding
import com.example.ticititacititoe.game.MultiplayerGameInvitationFragment
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.example.ticititacititoe.game.ui.OutgoingInviteFragment
import com.example.ticititacititoe.game.ui.QueueFragment
import com.example.ticititacititoe.leaderboard.ViewPagerAdapter
import com.example.ticititacititoe.profile.UserViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding

    private lateinit var userViewModel: UserViewModel
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
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
        val fragments = listOf(
            GlobalLeaderboardFragment(),
            FriendsLeaderboardFragment()
        )

        val adapter = ViewPagerAdapter(fragments,
            supportFragmentManager,
            lifecycle)

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.containerTabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.global)
                1 -> getString(R.string.friends)
                else -> ""
            }
        }.attach()

        binding.backButton.setOnClickListener {
            finish()
        }
    }


}