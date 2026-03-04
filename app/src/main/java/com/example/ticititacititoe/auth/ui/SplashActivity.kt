package com.example.ticititacititoe.auth.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.MainActivity
import com.example.ticititacititoe.R
import com.example.ticititacititoe.auth.state.AuthUiState
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.databinding.ActivitySplashBinding
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        Handler(Looper.getMainLooper()).postDelayed({

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    authViewModel.authUiState.collect { state ->
                        when (state) {
                            is AuthUiState.LoggedIn -> {
                                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                            is AuthUiState.LoggedOut -> {
                                val intent = Intent(this@SplashActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                            is AuthUiState.Loading -> {
                            }
                        }
                    }
                }
            }
       },  1000)





    }
}