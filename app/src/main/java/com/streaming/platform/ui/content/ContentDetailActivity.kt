package com.streaming.platform.ui.content

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.streaming.platform.api.ApiHelper
import com.streaming.platform.databinding.ActivityContentDetailBinding

/**
 * Content Detail Activity - Shows detailed information about a content item
 */
class ContentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContentDetailBinding
    private lateinit var apiHelper: ApiHelper

    private var contentId: Int = 0
    private var contentTitle: String = ""
    private var contentDescription: String = ""
    private var contentFilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiHelper = ApiHelper(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadContentData()
        setupListeners()
    }

    private fun loadContentData() {
        contentId = intent.getIntExtra("content_id", 0)
        contentTitle = intent.getStringExtra("content_title") ?: ""
        contentDescription = intent.getStringExtra("content_description") ?: ""
        contentFilePath = intent.getStringExtra("content_file_path") ?: ""

        supportActionBar?.title = contentTitle

        binding.tvTitle.text = contentTitle
        binding.tvDescription.text = contentDescription.ifEmpty { "No description available" }
        binding.tvFilePath.text = "File: $contentFilePath"
    }

    private fun setupListeners() {
        binding.btnPlayVideo.setOnClickListener {
            // Navigate to video player
            val intent = Intent(this, VideoPlayerActivity::class.java)
            intent.putExtra("content_id", contentId)
            intent.putExtra("content_title", contentTitle)
            intent.putExtra("video_url", getVideoUrl())
            startActivity(intent)
        }
    }

    private fun getVideoUrl(): String {
        // Get the streaming URL from ApiHelper
        return apiHelper.getStreamUrl(contentId)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}