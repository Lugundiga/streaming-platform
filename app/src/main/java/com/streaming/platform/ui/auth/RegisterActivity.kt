package com.streaming.platform.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.streaming.platform.api.ApiHelper
import com.streaming.platform.databinding.ActivityRegisterBinding
import com.streaming.platform.utils.Utils

/**
 * Registration Activity for new users
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiHelper = ApiHelper(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register"

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInputs(username, email, password, confirmPassword)) {
                performRegistration(username, email, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Username is required"
                false
            }
            username.length < 3 -> {
                binding.etUsername.error = "Username must be at least 3 characters"
                false
            }
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
            !Utils.isValidPassword(password) -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Please confirm password"
                false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun performRegistration(username: String, email: String, password: String) {
        showLoading(true)

        apiHelper.register(
            username = username,
            email = email,
            password = password,
            onSuccess = { apiResponse ->
                runOnUiThread {
                    showLoading(false)

                    if (apiResponse.error != null) {
                        Utils.showToast(this, apiResponse.error)
                    } else {
                        Utils.showToast(this, "Registration successful! Please login.")
                        finish() // Go back to login
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
        binding.btnRegister.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}