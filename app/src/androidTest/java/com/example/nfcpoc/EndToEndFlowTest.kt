package com.example.nfcpoc

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nfcpoc.data.local.AppDatabase
import com.example.nfcpoc.data.remote.ApiClient
import com.example.nfcpoc.data.repository.TagRepository
import com.example.nfcpoc.ui.AppScaffold
import com.example.nfcpoc.ui.AppTags
import com.example.nfcpoc.ui.DebugPanelTags
import com.example.nfcpoc.ui.screens.HistoryTags
import com.example.nfcpoc.ui.screens.ReadTagTags
import com.example.nfcpoc.ui.viewmodel.TagViewModel
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class EndToEndFlowTest {

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

        dbFile = File(context.cacheDir, "e2e-${System.nanoTime()}.db")
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
    fun readingTag_persistsToRoomAndUpdatesUi() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"id":"abc-123","processedAt":"2026-04-19T12:00:00Z","data":"ok:04A1B2C3"}""")
        )

        composeRule.setContent { AppScaffold(viewModel = viewModel) }

        viewModel.onTagRead("04A1B2C3")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Leitura salva").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(ReadTagTags.STATUS).assertIsDisplayed()

        composeRule.onNodeWithTag(AppTags.TAB_HISTORY).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("04A1B2C3").fetchSemanticsNodes().isNotEmpty()
        }

        val rows = database.tagDao().getAll()
        assertEquals(1, rows.size)
        assertEquals("04A1B2C3", rows[0].tagValue)
        assertEquals("ok:04A1B2C3", rows[0].apiResponse)
        assertEquals("SUCCESS", rows[0].syncStatus)

        val recorded = server.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/tags", recorded.path)
        assertTrue(recorded.body.readUtf8().contains("04A1B2C3"))
    }

    @Test
    fun apiFailure_persistsAsFailed() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        composeRule.setContent { AppScaffold(viewModel = viewModel) }

        viewModel.onTagRead("DEADBEEF")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Falha").fetchSemanticsNodes().isNotEmpty()
        }

        val rows = database.tagDao().getAll()
        assertEquals(1, rows.size)
        assertEquals("DEADBEEF", rows[0].tagValue)
        assertEquals("FAILED", rows[0].syncStatus)

        composeRule.onNodeWithTag(AppTags.TAB_HISTORY).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("DEADBEEF").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(HistoryTags.row(rows[0].id)).assertIsDisplayed()
    }

    @Test
    fun debugPanel_showsApiUrlAndRawValue_whenEnabled() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"ok"}"""))

        val apiUrl = server.url("/").toString()
        composeRule.setContent {
            AppScaffold(viewModel = viewModel, debugEnabled = true, apiBaseUrl = apiUrl)
        }

        composeRule.onNodeWithTag(DebugPanelTags.ROOT).assertIsDisplayed()
        composeRule.onNodeWithTag(DebugPanelTags.API_URL).assertTextContains(apiUrl, substring = true)

        viewModel.onTagRead("AA11BB22")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("AA11BB22", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(DebugPanelTags.LAST_VALUE)
            .assertTextContains("AA11BB22", substring = true)
    }

    @Test
    fun debugPanel_hidden_whenDisabled() = runTest {
        composeRule.setContent {
            AppScaffold(viewModel = viewModel, debugEnabled = false, apiBaseUrl = "http://whatever/")
        }
        composeRule.onAllNodesWithText("DEBUG").fetchSemanticsNodes().let {
            org.junit.Assert.assertTrue("Painel de debug não deveria aparecer", it.isEmpty())
        }
    }

    @Test
    fun clearHistory_removesAllRowsFromRoomAndUi() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"a"}"""))
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"b"}"""))

        repository.processTag("TAG-1").getOrThrow()
        repository.processTag("TAG-2").getOrThrow()
        assertEquals(2, database.tagDao().count())

        composeRule.setContent { AppScaffold(viewModel = viewModel, debugEnabled = false, apiBaseUrl = "") }

        composeRule.onNodeWithTag(AppTags.TAB_HISTORY).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("TAG-1", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(HistoryTags.CLEAR_BUTTON).performClick()
        composeRule.onNodeWithTag(HistoryTags.CLEAR_CONFIRM).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(HistoryTags.EMPTY).fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(0, database.tagDao().count())
    }

    @Test
    fun clearHistory_cancel_keepsRows() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data":"x"}"""))
        repository.processTag("KEEP-ME").getOrThrow()

        composeRule.setContent { AppScaffold(viewModel = viewModel, debugEnabled = false, apiBaseUrl = "") }
        composeRule.onNodeWithTag(AppTags.TAB_HISTORY).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("KEEP-ME", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(HistoryTags.CLEAR_BUTTON).performClick()
        composeRule.onNodeWithTag(HistoryTags.CLEAR_CANCEL).performClick()

        assertEquals(1, database.tagDao().count())
    }

    @Test
    fun dataSurvivesDatabaseReopen() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody("""{"data":"persisted"}""")
        )

        repository.processTag("CAFE").getOrThrow()

        database.close()

        val reopened = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
        try {
            val rows = reopened.tagDao().getAll()
            assertEquals(1, rows.size)
            assertEquals("CAFE", rows[0].tagValue)
            assertEquals("persisted", rows[0].apiResponse)
        } finally {
            reopened.close()
        }
    }
}
