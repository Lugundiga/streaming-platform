package com.streaming.platform.ui.content

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.streaming.platform.databinding.ActivityVideoPlayerBinding
import com.streaming.platform.utils.SessionManager
import com.streaming.platform.utils.Utils

/**
 * Video Player Activity - Streams and plays video content using Media3 ExoPlayer
 */
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var sessionManager: SessionManager

    private var videoUrl: String = ""
    private var contentTitle: String = ""
    private val TAG = "VideoPlayer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Hide action bar for fullscreen
        supportActionBar?.hide()

        loadVideoData()
        setupExoPlayer()
    }

    private fun loadVideoData() {
        contentTitle = intent.getStringExtra("content_title") ?: "Video"
        videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""

        Log.d(TAG, "Attempting to play: $videoUrl")
        binding.tvVideoTitle.text = contentTitle
    }

    @OptIn(UnstableApi::class)
    private fun setupExoPlayer() {
        if (videoUrl.isEmpty()) {
            Utils.showToast(this, "Error: Invalid video URL")
            finish()
            return
        }

        val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setUserAgent(userAgent)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(httpDataSourceFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        binding.playerView.player = player

        // Use URL as-is
        val mediaItem = MediaItem.fromUri(videoUrl)
        player?.setMediaItem(mediaItem)

        player?.apply {
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> binding.progressBar.visibility = View.VISIBLE
                        Player.STATE_READY -> {
                            binding.progressBar.visibility = View.GONE
                            Log.d(TAG, "Playback ready. Format: ${player?.videoFormat}")
                        }
                        Player.STATE_ENDED -> Utils.showToast(this@VideoPlayerActivity, "Finished")
                        else -> binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    binding.progressBar.visibility = View.GONE
                    Log.e(TAG, "ExoPlayer Error: ${error.message}")
                    Utils.showToast(this@VideoPlayerActivity, "Playback error: ${error.message}", Toast.LENGTH_LONG)
                }
            })
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onStop() { super.onStop(); releasePlayer() }
    override fun onDestroy() { super.onDestroy(); releasePlayer() }
}
