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
                "Server Error $statusCode"
            }
            error.message != null -> {
                Log.e(TAG, "Error: ${error.message}")
                error.message!!
            }
            else -> "Network error"
        }
        onError(message)
    }

    fun login(email: String, password: String, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/login.php"
        val jsonBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d(TAG, "Login response: $response")
                try {
                    val json = JSONObject(response)
                    if (json.has("error")) {
                        onError(json.getString("error"))
                    } else {
                        val role = json.optString("role", "user")
                        val token = json.optString("token", "")
                        onSuccess(ApiResponse(role = role, token = token))
                    }
                } catch (e: Exception) {
                    onError("Parsing error")
                }
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun register(username: String, email: String, password: String, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/register.php"
        val jsonBody = JSONObject().apply {
            put("username", username)
            put("email", email)
            put("password", password)
        }

        val request = object : StringRequest(Method.POST, url,
            { response ->
                Log.d(TAG, "Register response: $response")
                try {
                    val json = JSONObject(response)
                    if (json.has("error")) {
                        // This handles the "All fields required" or "User already exists" cases
                        onError(json.getString("error"))
                    } else {
                        onSuccess(ApiResponse(Unit))
                    }
                } catch (e: Exception) {
                    onError("Parsing error")
                }
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
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
        val jsonBody = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("file_path", filePath)
        }
        val request = object : StringRequest(Method.POST, url,
            { response -> 
                try {
                    val json = JSONObject(response)
                    if (json.has("error")) onError(json.getString("error"))
                    else onSuccess(ApiResponse(Unit))
                } catch (e: Exception) { onSuccess(ApiResponse(Unit)) }
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun updateContent(id: Int, title: String, description: String, filePath: String?, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/update_content.php?id=$id"
        val jsonBody = JSONObject().apply {
            put("title", title)
            put("description", description)
            filePath?.let { put("file_path", it) }
        }
        val request = object : StringRequest(Method.POST, url,
            { response -> onSuccess(ApiResponse(Unit)) },
            { error -> handleVolleyError(error, onError) }) {
            override fun getBodyContentType(): String = "application/json; charset=utf-8"
            override fun getBody(): ByteArray = jsonBody.toString().toByteArray()
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun deleteContent(id: Int, onSuccess: (ApiResponse<Unit>) -> Unit, onError: (String) -> Unit) {
        val url = "$baseUrl/api/delete_content.php?id=$id"
        val request = object : StringRequest(Method.GET, url,
            { response -> onSuccess(ApiResponse(Unit)) },
            { error -> handleVolleyError(error, onError) }) {
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    fun getStreamUrl(id: Int): String {
        return "$baseUrl/api/stream.php?id=$id"
    }
}
