package com.streaming.platform.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_TOKEN = "token"
    }

    fun createSession(userId: String, username: String, role: String, token: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
            putString(KEY_TOKEN, token)
            apply()
        }
    }

    fun saveSession(username: String, role: String, token: String? = null) {
        createSession(username, username, role, token)
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserRole(): String? = prefs.getString(KEY_ROLE, null)
    
    fun isAdmin(): Boolean = getUserRole() == "admin"

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun logout() {
        clearSession()
    }
}
