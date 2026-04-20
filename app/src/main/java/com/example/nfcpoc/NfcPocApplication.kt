package com.example.nfcpoc

import android.app.Application
import com.example.nfcpoc.data.local.AppDatabase
import com.example.nfcpoc.data.remote.ApiClient
import com.example.nfcpoc.data.remote.ApiService
import com.example.nfcpoc.data.repository.TagRepository

class NfcPocApplication : Application(), RepositoryProvider {

    private val apiService: ApiService by lazy { ApiClient.create() }
    private val database by lazy { AppDatabase.get(this) }

    override val repository: TagRepository by lazy {
        TagRepository(apiService, database.tagDao())
    }
}

interface RepositoryProvider {
    val repository: TagRepository
}
