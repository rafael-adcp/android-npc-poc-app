package com.example.nfcpoc.data.remote

import com.example.nfcpoc.data.remote.dto.TagRequest
import com.example.nfcpoc.data.remote.dto.TagResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("tags")
    suspend fun postTag(@Body request: TagRequest): TagResponse
}
