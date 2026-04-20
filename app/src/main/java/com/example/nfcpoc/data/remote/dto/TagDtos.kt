package com.example.nfcpoc.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagRequest(
    val tagValue: String,
    val readAt: Long
)

@Serializable
data class TagResponse(
    val id: String? = null,
    val processedAt: String? = null,
    val data: String? = null
)
