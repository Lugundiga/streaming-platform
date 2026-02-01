package com.streaming.platform.api

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.streaming.platform.models.Content
import org.json.JSONObject

class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val role: String? = null,
    val token: String? = null
)

class ApiHelper(private val context: Context) {
    private val volleyClient = VolleyClient.getInstance(context)
    private var authToken: String? = null
    private val TAG = "ApiHelper"

    // Base URL. Make sure this IP is correct.
    private val baseUrl = "http://192.168.1.164/streaming_platform" 

    fun setAuthToken(token: String?) {
        this.authToken = token
    }

    private fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        authToken?.let { headers["Authorization"] = "Bearer $it" }
        return headers
    }

    private fun handleVolleyError(error: VolleyError, onError: (String) -> Unit) {
        val statusCode = error.networkResponse?.statusCode
        val message = when {
            error.networkResponse != null -> {
                val data = error.networkResponse.data
                val responseBody = if (data != null) String(data) else ""
                Log.e(TAG, "Error $statusCode: $responseBody")
                "Server Error $statusCode: $responseBody"
            }
            error.message != null -> {
                Log.e(TAG, "Error: ${error.message}")
                error.message!!
            }
            else -> {
                Log.e(TAG, "Unknown Volley Error")
                "Network error or server unreachable"
            }
        }
        onError(message)
    }

    fun login(email: String, password: String, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        // Appending .php if you are using standard PHP files without a router
        val url = "$baseUrl/api/login.php"
        Log.d(TAG, "Login request to: $url")
        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d(TAG, "Login response: $response")
                try {
                    val json = JSONObject(response)
                    val role = json.optString("role", "user")
                    val token = json.optString("token", "")
                    onSuccess(ApiResponse(role = role, token = token))
                } catch (e: Exception) {
                    Log.e(TAG, "Login parse error", e)
                    onError("Parsing error")
                }
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                return params
            }
        }
        volleyClient.addToRequestQueue(request)
    }

    fun register(username: String, email: String, password: String, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        // Appending .php if you are using standard PHP files without a router
        val url = "$baseUrl/api/register.php"
        Log.d(TAG, "Register request to: $url")
        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d(TAG, "Register response: $response")
                onSuccess(ApiResponse(Unit)) 
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username
                params["email"] = email
                params["password"] = password
                return params
            }
        }
        volleyClient.addToRequestQueue(request)
    }

    fun listContent(onSuccess: (List<Content>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/list_content.php"
        val request = object : StringRequest(Method.GET, url,
            { response ->
                try {
                    val contents = Gson().fromJson(response, Array<Content>::class.java).toList()
                    onSuccess(contents)
                } catch (e: Exception) {
                    onError("Parsing error")
                }
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun addContent(title: String, description: String, filePath: String, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/add_content.php"
        val request = object : StringRequest(Method.POST, url,
            { onSuccess(ApiResponse(Unit)) },
            { error -> handleVolleyError(error, onError) }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["title"] = title
                params["description"] = description
                params["file_path"] = filePath
                return params
            }
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun updateContent(id: Int, title: String, description: String, filePath: String?, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/update_content.php?id=$id"
        val request = object : StringRequest(Method.POST, url, // Using POST for update often easier with PHP
            { onSuccess(ApiResponse(Unit)) },
            { error -> handleVolleyError(error, onError) }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["title"] = title
                params["description"] = description
                filePath?.let { params["file_path"] = it }
                return params
            }
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun deleteContent(id: Int, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/delete_content.php?id=$id"
        val request = object : StringRequest(Method.GET, url, // Using GET for delete is common in simple scripts
            { onSuccess(ApiResponse(Unit)) },
            { error -> handleVolleyError(error, onError) }) {
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun getStreamUrl(id: Int): String {
        return "$baseUrl/api/stream.php?id=$id"
    }
}
