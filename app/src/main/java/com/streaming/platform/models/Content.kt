package com.streaming.platform.models

import com.google.gson.annotations.SerializedName

data class Content(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("file_path")
    val filePath: String? = null,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerializedName("category")
    val category: String? = null
)
