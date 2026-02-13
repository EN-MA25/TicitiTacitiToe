package com.example.ticititacititoe.auth.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.R
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    // =========== ViewBinding ============
    lateinit var binding: ActivityRegisterBinding

    // =========== ViewModel ============
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // =========== Setup ViewBinding ============
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // =========== Initilize ViewModel ============
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        // =========== Register buttonclick ============
        binding.registerButton.setOnClickListener {

            // =========== Check input validation ============
            if (checkValidInput()) {

                // =========== Get user input ============
                val username = binding.usernameEditText.text.toString()
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                // =========== Call ViewModel and check registration success ============
                viewModel.registerUser(username, email, password) { result ->
                    result.onSuccess {
                        clearFields()
                        Toast.makeText(
                            this,
                            "Registration was successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // =========== Navigate to LoginActivity ============
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra("email", email)
                        intent.putExtra("password", password)
                        startActivity(intent)
                        finish()
                    }

                    // =========== Registration failed ============
                    result.onFailure {
                        Toast.makeText(
                            this,
                            it.message ?: "Registration failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
        }

        binding.loginHereButton.setOnClickListener {
            finish()
        }
    }

    // =========== Input validation ============
    fun checkValidInput(): Boolean {
        val username = binding.usernameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        if (username.isEmpty()) {
            Toast.makeText(this, "Username can not be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "Email can not be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password can not be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be atleast 6 letters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // =========== Clear input fields ============
    fun clearFields() {
        binding.usernameEditText.text.clear()
        binding.emailEditText.text.clear()
        binding.passwordEditText.text.clear()
    }
}
