package com.streaming.platform.ui.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
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
    private var selectedVideoUri: Uri? = null

    // Video Picker Launcher
    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedVideoUri = result.data?.data
            selectedVideoUri?.let { uri ->
                val fileName = getFileName(uri)
                binding.tvSelectedFile.text = "Selected: $fileName"
                binding.tvSelectedFile.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        sessionManager = SessionManager(this)
        apiHelper = ApiHelper(this)

        // Set token for API calls
        apiHelper.setAuthToken(sessionManager.getToken())

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Content"

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSelectVideo.setOnClickListener {
            openVideoPicker()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (validateInputs(title)) {
                uploadVideoAndSave(title, description)
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        pickVideoLauncher.launch(Intent.createChooser(intent, "Select Video"))
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Unknown"
    }

    private fun validateInputs(title: String): Boolean {
        return when {
            title.isEmpty() -> {
                binding.etTitle.error = "Title is required"
                false
            }
            selectedVideoUri == null -> {
                Utils.showToast(this, "Please select a video file")
                false
            }
            else -> true
        }
    }

    private fun uploadVideoAndSave(title: String, description: String) {
        showLoading(true)
        
        selectedVideoUri?.let { uri ->
            apiHelper.uploadVideo(uri, 
                onSuccess = { videoUrl ->
                    // Video uploaded successfully, now add the record to DB
                    apiHelper.addContent(
                        title = title,
                        description = description,
                        filePath = videoUrl,
                        onSuccess = { apiResponse ->
                            runOnUiThread {
                                showLoading(false)
                                Utils.showToast(this, "Content added successfully!")
                                finish()
                            }
                        },
                        onError = { error ->
                            runOnUiThread {
                                showLoading(false)
                                Utils.showToast(this, "Error adding content: $error")
                            }
                        }
                    )
                },
                onError = { error ->
                    runOnUiThread {
                        showLoading(false)
                        Utils.showToast(this, "Upload error: $error")
                    }
                }
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading
        binding.btnSelectVideo.isEnabled = !isLoading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
