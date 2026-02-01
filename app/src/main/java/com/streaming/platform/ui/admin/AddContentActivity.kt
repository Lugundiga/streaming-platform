package com.streaming.platform.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.streaming.platform.api.ApiHelper
import com.streaming.platform.databinding.ActivityAddContentBinding
import com.streaming.platform.utils.SessionManager
import com.streaming.platform.utils.Utils

/**
 * Add Content Activity - For admin users to add new content
 */
class AddContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContentBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiHelper: ApiHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiHelper = ApiHelper(this)

        // Set token for API calls
        apiHelper.setAuthToken(sessionManager.getToken())

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Content"

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val filePath = binding.etFilePath.text.toString().trim()

            if (validateInputs(title, filePath)) {
                addContent(title, description, filePath)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(title: String, filePath: String): Boolean {
        return when {
            title.isEmpty() -> {
                binding.etTitle.error = "Title is required"
                false
            }
            filePath.isEmpty() -> {
                binding.etFilePath.error = "File path is required"
                false
            }
            else -> true
        }
    }

    private fun addContent(title: String, description: String, filePath: String) {
        showLoading(true)

        apiHelper.addContent(
            title = title,
            description = description,
            filePath = filePath,
            onSuccess = { apiResponse ->
                runOnUiThread {
                    showLoading(false)

                    if (apiResponse.error != null) {
                        Utils.showToast(this, apiResponse.error)
                    } else {
                        Utils.showToast(this, "Content added successfully!")
                        finish() // Go back to main activity
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
        binding.btnSave.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}