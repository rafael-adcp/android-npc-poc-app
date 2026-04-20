package com.example.nfcpoc.data.repository

import com.example.nfcpoc.data.local.TagDao
import com.example.nfcpoc.data.local.TagEntity
import com.example.nfcpoc.data.remote.ApiService
import com.example.nfcpoc.data.remote.dto.TagRequest
import kotlinx.coroutines.flow.Flow

class TagRepository(
    private val api: ApiService,
    private val dao: TagDao
) {

    fun observeHistory(): Flow<List<TagEntity>> = dao.observeAll()

    suspend fun clearHistory() = dao.deleteAll()

    suspend fun processTag(tagValue: String): Result<TagEntity> {
        val now = System.currentTimeMillis()
        return runCatching { api.postTag(TagRequest(tagValue, now)) }
            .map { response ->
                val entity = TagEntity(
                    tagValue = tagValue,
                    apiResponse = response.data ?: response.id,
                    readAt = now,
                    syncStatus = TagEntity.STATUS_SUCCESS
                )
                val id = dao.insert(entity)
                entity.copy(id = id)
            }
            .recoverCatching { error ->
                val entity = TagEntity(
                    tagValue = tagValue,
                    apiResponse = error.message,
                    readAt = now,
                    syncStatus = TagEntity.STATUS_FAILED
                )
                val id = dao.insert(entity)
                throw TagProcessingException(entity.copy(id = id), error)
            }
    }
}

class TagProcessingException(
    val persistedEntity: TagEntity,
    cause: Throwable
) : Exception(cause.message, cause)
