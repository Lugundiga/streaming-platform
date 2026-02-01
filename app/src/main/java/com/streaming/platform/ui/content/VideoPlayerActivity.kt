package com.streaming.platform.ui.content

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.streaming.platform.databinding.ActivityVideoPlayerBinding
import com.streaming.platform.utils.Utils

/**
 * Video Player Activity - Streams and plays video content using ExoPlayer
 */
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null

    private var videoUrl: String = ""
    private var contentTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar for fullscreen
        supportActionBar?.hide()

        loadVideoData()
        initializePlayer()
    }

    private fun loadVideoData() {
        contentTitle = intent.getStringExtra("content_title") ?: ""
        videoUrl = intent.getStringExtra("video_url") ?: ""

        binding.tvVideoTitle.text = contentTitle
    }

    private fun initializePlayer() {
        // Create ExoPlayer instance
        player = ExoPlayer.Builder(this).build()

        // Bind player to view
        binding.playerView.player = player

        // Create media item from URL
        val mediaItem = MediaItem.fromUri(videoUrl)

        // Set media item and prepare
        player?.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            // Add listener for player events
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        Player.STATE_ENDED -> {
                            Utils.showToast(this@VideoPlayerActivity, "Video ended")
                        }
                        else -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    binding.progressBar.visibility = View.GONE
                    Utils.showToast(
                        this@VideoPlayerActivity,
                        "Playback error: ${error.message}"
                    )
                }
            })
        }
    }

    private fun releasePlayer() {
        player?.apply {
            playWhenReady = false
            release()
        }
        player = null
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onBackPressed() {
        releasePlayer()
        super.onBackPressed()
    }
}