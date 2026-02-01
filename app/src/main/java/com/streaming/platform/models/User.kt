package com.streaming.platform.models

import com.google.gson.annotations.SerializedName

/**
 * User data model
 */
data class User(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null
)

/**
 * Login request model
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Login response model
 */
data class LoginResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("token")
    val token: String? = null,

    @SerializedName("error")
    val error: String? = null
)

/**
 * Register request model
 */
data class RegisterRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * Generic API response
 */
data class ApiResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("id")
    val id: Int? = null
)