package com.example.ticititacititoe.leaderboard.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.ActivityLeaderboardBinding
import com.example.ticititacititoe.leaderboard.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
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