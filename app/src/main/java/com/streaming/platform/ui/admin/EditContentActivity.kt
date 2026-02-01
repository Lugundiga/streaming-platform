package com.streaming.platform.ui.admin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.streaming.platform.api.ApiHelper
import com.streaming.platform.databinding.ActivityEditContentBinding
import com.streaming.platform.utils.SessionManager
import com.streaming.platform.utils.Utils

/**
 * Edit Content Activity - For admin users to edit existing content
 */
class EditContentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditContentBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiHelper: ApiHelper

    private var contentId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        apiHelper = ApiHelper(this)

        // Set token for API calls
        apiHelper.setAuthToken(sessionManager.getToken())

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Content"

        loadContentData()
        setupListeners()
    }

    private fun loadContentData() {
        contentId = intent.getIntExtra("content_id", 0)
        val title = intent.getStringExtra("content_title") ?: ""
        val description = intent.getStringExtra("content_description") ?: ""
        val filePath = intent.getStringExtra("content_file_path") ?: ""

        binding.etTitle.setText(title)
        binding.etDescription.setText(description)
        binding.etFilePath.setText(filePath)
    }

    private fun setupListeners() {
        binding.btnUpdate.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val filePath = binding.etFilePath.text.toString().trim()

            if (validateInputs(title)) {
                updateContent(title, description, filePath)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun validateInputs(title: String): Boolean {
        return when {
            title.isEmpty() -> {
                binding.etTitle.error = "Title is required"
                false
            }
            else -> true
        }
    }

    private fun updateContent(title: String, description: String, filePath: String) {
        showLoading(true)

        apiHelper.updateContent(
            id = contentId,
            title = title,
            description = description,
            filePath = filePath.ifEmpty { null },
            onSuccess = { apiResponse ->
                runOnUiThread {
                    showLoading(false)

                    if (apiResponse.error != null) {
                        Utils.showToast(this, apiResponse.error)
                    } else {
                        Utils.showToast(this, "Content updated successfully!")
                        finish() // Go back
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
        binding.btnUpdate.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}