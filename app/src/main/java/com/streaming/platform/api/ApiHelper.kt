package com.streaming.platform.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.streaming.platform.models.Content
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class ApiResponse<T>(
    val data: T? = null,
    val error: String? = null,
    val role: String? = null,
    val token: String? = null,
    val file_path: String? = null
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
                Log.d(TAG, "Content list response: $response")
                try {
                    val contents = Gson().fromJson(response, Array<Content>::class.java).toList()
                    // Log the parsed content list to verify file paths
                    contents.forEach { Log.d(TAG, "Parsed Content: ID=${it.id}, Title=${it.title}, FilePath=${it.filePath}") }
                    onSuccess(contents)
                } catch (e: Exception) {
                    Log.e(TAG, "Gson Parsing Error", e)
                    onError("Parsing error")
                }
            },
            { error -> handleVolleyError(error, onError) }) {
            override fun getHeaders(): MutableMap<String, String> = this@ApiHelper.getHeaders()
        }
        volleyClient.addToRequestQueue(request)
    }

    /**
     * Uploads a video file to the server using the upload_video.php script.
     * This uses HttpURLConnection for a multipart/form-data request.
     */
    fun uploadVideo(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        Thread {
            try {
                val url = URL("$baseUrl/api/upload_video.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.useCaches = false
                connection.requestMethod = "POST"
                
                val boundary = "*****" + System.currentTimeMillis() + "*****"
                connection.setRequestProperty("Connection", "Keep-Alive")
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                
                val outputStream = DataOutputStream(connection.outputStream)

                // Add File parameter
                outputStream.writeBytes("--$boundary\r\n")
                outputStream.writeBytes("Content-Disposition: form-data; name=\"video\"; filename=\"video.mp4\"\r\n")
                outputStream.writeBytes("Content-Type: video/mp4\r\n\r\n")
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                inputStream?.close()

                outputStream.writeBytes("\r\n")
                outputStream.writeBytes("--$boundary--\r\n")
                outputStream.flush()
                outputStream.close()
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val json = JSONObject(response.toString())
                    if (json.getBoolean("success")) {
                        onSuccess(json.getString("file_path"))
                    } else {
                        onError(json.optString("error", "Upload failed"))
                    }
                } else {
                    onError("Server error: $responseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Upload exception", e)
                onError(e.message ?: "Unknown upload error")
            }
        }.start()
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
