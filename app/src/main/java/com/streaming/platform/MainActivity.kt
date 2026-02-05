package com.streaming.platform

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.streaming.platform.api.ApiHelper
import com.streaming.platform.databinding.ActivityMainBinding
import com.streaming.platform.models.Content
import com.streaming.platform.ui.admin.AddContentActivity
import com.streaming.platform.ui.auth.LoginActivity
import com.streaming.platform.ui.content.ContentAdapter
import com.streaming.platform.ui.content.ContentDetailActivity
import com.streaming.platform.utils.SessionManager
import com.streaming.platform.utils.Utils

/**
 * Main Activity - Displays content list based on user role
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var apiHelper: ApiHelper
    private lateinit var contentAdapter: ContentAdapter
    private val contentList = mutableListOf<Content>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        sessionManager = SessionManager(this)
        apiHelper = ApiHelper(this)

        // Set token for API calls
        apiHelper.setAuthToken(sessionManager.getToken())

        setupUI()
        setupRecyclerView()
        loadContent()
    }

    private fun setupUI() {
        // Set toolbar title based on role
        supportActionBar?.title = if (sessionManager.isAdmin()) {
            "Content Management"
        } else {
            "Browse Content"
        }

        // Show FAB only for admin users
        if (sessionManager.isAdmin()) {
            binding.fabAddContent.visibility = View.VISIBLE
            binding.fabAddContent.setOnClickListener {
                startActivity(Intent(this, AddContentActivity::class.java))
            }
        } else {
            binding.fabAddContent.visibility = View.GONE
        }

        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            loadContent()
        }
    }

    private fun setupRecyclerView() {
        contentAdapter = ContentAdapter(
            contentList = contentList,
            isAdmin = sessionManager.isAdmin(),
            onItemClick = { content ->
                // Navigate to content detail
                navigateToContentDetail(content)
            },
            onEditClick = { content ->
                // Edit content (admin only)
                navigateToEditContent(content)
            },
            onDeleteClick = { content ->
                // Delete content (admin only)
                confirmDeleteContent(content)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contentAdapter
        }
    }

    private fun loadContent() {
        showLoading(true)

        apiHelper.listContent(
            onSuccess = { contents ->
                runOnUiThread {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false

                    contentList.clear()
                    contentList.addAll(contents)
                    contentAdapter.notifyDataSetChanged()

                    // Show empty state if no content
                    binding.tvEmptyState.visibility =
                        if (contents.isEmpty()) View.VISIBLE else View.GONE
                }
            },
            onError = { error ->
                runOnUiThread {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    Utils.showToast(this, "Error: $error")
                }
            }
        )
    }

    private fun navigateToContentDetail(content: Content) {
        val intent = Intent(this, ContentDetailActivity::class.java)
        intent.putExtra("content_id", content.id)
        intent.putExtra("content_title", content.title)
        intent.putExtra("content_description", content.description)
        intent.putExtra("content_file_path", content.filePath)
        startActivity(intent)
    }

    private fun navigateToEditContent(content: Content) {
        val intent = Intent(this, com.streaming.platform.ui.admin.EditContentActivity::class.java)
        intent.putExtra("content_id", content.id)
        intent.putExtra("content_title", content.title)
        intent.putExtra("content_description", content.description)
        intent.putExtra("content_file_path", content.filePath)
        startActivity(intent)
    }

    private fun confirmDeleteContent(content: Content) {
        Utils.showConfirmDialog(
            context = this,
            title = "Delete Content",
            message = "Are you sure you want to delete '${content.title}'?",
            positiveAction = {
                deleteContent(content)
            }
        )
    }

    private fun deleteContent(content: Content) {
        showLoading(true)

        apiHelper.deleteContent(
            id = content.id ?: 0,
            onSuccess = { apiResponse ->
                runOnUiThread {
                    showLoading(false)

                    if (apiResponse.error != null) {
                        Utils.showToast(this, apiResponse.error)
                    } else {
                        Utils.showToast(this, "Content deleted successfully")
                        loadContent() // Refresh list
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
    }

    override fun onResume() {
        super.onResume()
        loadContent() // Refresh content when returning to this activity
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_refresh -> {
                loadContent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        Utils.showConfirmDialog(
            context = this,
            title = "Logout",
            message = "Are you sure you want to logout?",
            positiveAction = {
                sessionManager.clearSession()
                apiHelper.setAuthToken(null)

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        )
    }
}
