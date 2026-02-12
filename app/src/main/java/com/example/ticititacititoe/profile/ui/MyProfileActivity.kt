package com.example.ticititacititoe.profile.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.ActivityMyProfileBinding
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.profile.UserViewModel

class MyProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyProfileBinding

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

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.settingsButton.setOnClickListener {
            val dialog = SettingsFragment()
            dialog.show(supportFragmentManager, "settings_fragment_dialog")
        }

        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.fetchCurrentUser()

        userViewModel.currentUser.observe(this) { user ->
            if (user != null) {
                binding.usernameTextView.text = user.username
                binding.initialsTextView.text = user.username.first().toString()
            }
        }

    }
}
