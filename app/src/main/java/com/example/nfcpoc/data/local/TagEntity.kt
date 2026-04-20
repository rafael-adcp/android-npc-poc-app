package com.example.nfcpoc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_reads")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tagValue: String,
    val apiResponse: String?,
    val readAt: Long,
    val syncStatus: String
) {
    companion object {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_PENDING = "PENDING"
        const val STATUS_FAILED = "FAILED"
    }
}
