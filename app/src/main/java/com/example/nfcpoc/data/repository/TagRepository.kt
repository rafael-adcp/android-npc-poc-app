package com.example.nfcpoc.data.repository

import com.example.nfcpoc.data.local.TagDao
import com.example.nfcpoc.data.local.TagEntity
import com.example.nfcpoc.data.remote.ApiService
import com.example.nfcpoc.data.remote.dto.TagRequest
import com.example.nfcpoc.data.remote.dto.TagResponse
import kotlinx.coroutines.flow.Flow

class TagRepository(
    private val api: ApiService,
    private val dao: TagDao
) {

    fun observeHistory(): Flow<List<TagEntity>> = dao.observeAll()

    suspend fun clearHistory() = dao.deleteAll()

    suspend fun processTag(tagValue: String): Result<TagEntity> {
        val now = System.currentTimeMillis()
        return callApi(tagValue, now)
            .map { response ->
                val entity = buildSuccessEntity(tagValue, response, now)
                entity.copy(id = dao.insert(entity))
            }
            .recoverCatching { error ->
                val entity = buildFailureEntity(tagValue, error, now)
                throw TagProcessingException(entity.copy(id = dao.insert(entity)), error)
            }
    }

    private suspend fun callApi(tagValue: String, readAt: Long): Result<TagResponse> =
        runCatching { api.postTag(TagRequest(tagValue, readAt)) }

    private fun buildSuccessEntity(tagValue: String, response: TagResponse, now: Long) = TagEntity(
        tagValue = tagValue,
        apiResponse = response.data ?: response.id,
        readAt = now,
        syncStatus = TagEntity.STATUS_SUCCESS
    )

    private fun buildFailureEntity(tagValue: String, error: Throwable, now: Long) = TagEntity(
        tagValue = tagValue,
        apiResponse = error.message,
        readAt = now,
        syncStatus = TagEntity.STATUS_FAILED
    )
}

class TagProcessingException(
    val persistedEntity: TagEntity,
    cause: Throwable
) : Exception(cause.message, cause)
