package com.example.ticititacititoe.auth.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.MainActivity
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.databinding.ActivityLoginBinding
import com.example.ticititacititoe.game.ui.GameActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        emailEditText = binding.emailEditText
        passwordEditText = binding.passwordEditText


        binding.newPlayerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tryButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }


        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                binding.emailEditText.error = "Field can not be empty"
                binding.passwordEditText.error = "Field can not be empty"
                return@setOnClickListener

            }
            if (password.length < 6) {
                binding.passwordEditText.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
                login()

        }
    }
    
    fun login() {
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()
        authViewModel.login(email, password, onSuccess = {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }, onFailure = {
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })

    }


    fun clearFields() {
        binding.emailEditText.text?.clear()
        binding.passwordEditText.text?.clear()

    }
}
