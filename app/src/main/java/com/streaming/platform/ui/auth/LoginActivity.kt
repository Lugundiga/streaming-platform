package com.streaming.platform.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.streaming.platform.MainActivity
import com.streaming.platform.api.ApiHelper
import com.streaming.platform.databinding.ActivityLoginBinding
import com.streaming.platform.utils.SessionManager
import com.streaming.platform.utils.Utils

/**
 * Login Activity - Entry point of the application
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiHelper = ApiHelper(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        // Register link click
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = "Email is required"
                false
            }
            !Utils.isValidEmail(email) -> {
                binding.etEmail.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password is required"
                false
            }
            else -> true
        }
    }

    private fun performLogin(email: String, password: String) {
        showLoading(true)

        apiHelper.login(
            email = email,
            password = password,
            onSuccess = { loginResponse ->
                runOnUiThread {
                    showLoading(false)

                    if (loginResponse.error != null) {
                        Utils.showToast(this, loginResponse.error)
                    } else {
                        // Save session
                        val role = loginResponse.role ?: "user"
                        val token = loginResponse.token
                        
                        sessionManager.saveSession(email, role, token)

                        // Set token for API calls
                        apiHelper.setAuthToken(token)

                        Utils.showToast(this, "Login successful!")
                        navigateToMain()
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    showLoading(false)
                    Utils.showToast(this, "Error: $error")
                }
            }
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.tvRegister.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
