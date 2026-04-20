package com.example.nfcpoc

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nfcpoc.data.local.AppDatabase
import com.example.nfcpoc.data.remote.ApiClient
import com.example.nfcpoc.data.repository.TagRepository
import com.example.nfcpoc.ui.AppScaffold
import com.example.nfcpoc.ui.viewmodel.ReadUiState
import com.example.nfcpoc.ui.viewmodel.TagViewModel
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class TagViewModelEdgeCaseTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var server: MockWebServer
    private lateinit var database: AppDatabase
    private lateinit var dbFile: File
    private lateinit var repository: TagRepository
    private lateinit var viewModel: TagViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        server = MockWebServer().apply { start() }
        dbFile = File(context.cacheDir, "vm-test-${System.nanoTime()}.db")
        database = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        val api = ApiClient.create(server.url("/").toString())
        repository = TagRepository(api, database.tagDao())
        viewModel = TagViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
        dbFile.delete()
        server.shutdown()
    }

    @Test
    fun onTagRead_blankValue_isIgnored() = runTest {
        viewModel.onTagRead("   ")
        assertEquals(ReadUiState.Idle, viewModel.readState.value)
        assertNull(viewModel.lastRawValue.value)
    }

    @Test
    fun onTagRead_emptyString_isIgnored() = runTest {
        viewModel.onTagRead("")
        assertEquals(ReadUiState.Idle, viewModel.readState.value)
    }

    @Test
    fun resetState_goesBackToIdle() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"ok"}"""))

        composeRule.setContent { AppScaffold(viewModel = viewModel) }
        viewModel.onTagRead("TEST")

        composeRule.waitUntil(15_000) {
            viewModel.readState.value is ReadUiState.Success
        }

        viewModel.resetState()
        assertEquals(ReadUiState.Idle, viewModel.readState.value)
    }

    @Test
    fun onTagRead_setsLastRawValue() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"x"}"""))

        composeRule.setContent { AppScaffold(viewModel = viewModel) }
        viewModel.onTagRead("MY-TAG")

        composeRule.waitUntil(15_000) {
            viewModel.readState.value is ReadUiState.Success
        }

        assertEquals("MY-TAG", viewModel.lastRawValue.value)
    }

    @Test
    fun clearHistory_resetsLastRawValueAndState() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"x"}"""))

        composeRule.setContent { AppScaffold(viewModel = viewModel) }
        viewModel.onTagRead("TAG-1")

        composeRule.waitUntil(15_000) {
            viewModel.readState.value is ReadUiState.Success
        }

        viewModel.clearHistory()

        composeRule.waitUntil(15_000) {
            viewModel.readState.value == ReadUiState.Idle
        }

        assertNull(viewModel.lastRawValue.value)
        assertEquals(0, database.tagDao().count())
    }

    @Test
    fun onTagRead_apiError_producesErrorState() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        composeRule.setContent { AppScaffold(viewModel = viewModel) }
        viewModel.onTagRead("FAIL-TAG")

        composeRule.waitUntil(15_000) {
            viewModel.readState.value is ReadUiState.Error
        }

        val state = viewModel.readState.value as ReadUiState.Error
        assertTrue(state.message.isNotEmpty())
        assertEquals("FAIL-TAG", state.persisted?.tagValue)
        assertEquals("FAILED", state.persisted?.syncStatus)
    }

    @Test
    fun viewModelFactory_createsInstance() {
        val factory = TagViewModel.Factory(repository)
        val vm = factory.create(TagViewModel::class.java)
        assertEquals(ReadUiState.Idle, vm.readState.value)
    }
}
