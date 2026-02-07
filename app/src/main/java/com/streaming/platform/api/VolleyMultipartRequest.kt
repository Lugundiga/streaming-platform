package com.streaming.platform.api

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val boundary = "apiclient-${System.currentTimeMillis()}"
    private val lineEnd = "\r\n"
    private val twoHyphens = "--"

    override fun getHeaders(): MutableMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = "multipart/form-data; boundary=$boundary"
        return headers
    }

    override fun getBodyContentType(): String {
        return "multipart/form-data; boundary=$boundary"
    }

    override fun getBody(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = DataOutputStream(outputStream)

        // Add file data
        val data = getByteData()
        if (data != null && data.isNotEmpty()) {
            for ((key, datapart) in data) {
                writer.writeBytes(twoHyphens + boundary + lineEnd)
                writer.writeBytes("Content-Disposition: form-data; name=\"$key\"; filename=\"${datapart.fileName}\"$lineEnd")
                writer.writeBytes("Content-Type: ${datapart.type}$lineEnd")
                writer.writeBytes(lineEnd)
                writer.write(datapart.data)
                writer.writeBytes(lineEnd)
            }
        }

        // Add text parameters
        val params = getParams()
        if (params != null && params.isNotEmpty()) {
            for ((key, value) in params) {
                writer.writeBytes(twoHyphens + boundary + lineEnd)
                writer.writeBytes("Content-Disposition: form-data; name=\"$key\"$lineEnd")
                writer.writeBytes(lineEnd)
                writer.writeBytes(value)
                writer.writeBytes(lineEnd)
            }
        }

        // Close multipart
        writer.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
        writer.flush()
        writer.close()

        return outputStream.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
    }

    override fun deliverResponse(response: NetworkResponse) {
        listener.onResponse(response)
    }

    open fun getByteData(): Map<String, DataPart>? {
        return null
    }

    data class DataPart(
        val fileName: String,
        val data: ByteArray,
        val type: String
    )
}
