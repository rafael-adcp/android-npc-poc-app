package com.example.nfcpoc.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insert(entity: TagEntity): Long

    @Query("SELECT * FROM tag_reads ORDER BY readAt DESC")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tag_reads ORDER BY readAt DESC")
    suspend fun getAll(): List<TagEntity>

    @Query("SELECT COUNT(*) FROM tag_reads")
    suspend fun count(): Int

    @Query("DELETE FROM tag_reads")
    suspend fun deleteAll()
}
