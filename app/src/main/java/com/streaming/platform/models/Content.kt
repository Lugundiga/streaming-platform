package com.streaming.platform.models

data class Content(
    val id: Int? = null,
    val title: String,
    val description: String,
    val filePath: String? = null,
    val thumbnailUrl: String? = null,
    val category: String? = null
)
