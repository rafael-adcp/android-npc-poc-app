package com.example.nfcpoc

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nfcpoc.data.local.AppDatabase
import com.example.nfcpoc.data.local.TagEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class TagDaoEdgeCaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dbFile: File

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dbFile = File(context.cacheDir, "dao-test-${System.nanoTime()}.db")
        database = Room.databaseBuilder(context, AppDatabase::class.java, dbFile.absolutePath)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
        dbFile.delete()
    }

    private val dao get() = database.tagDao()

    @Test
    fun emptyDatabase_countIsZero() = runTest {
        assertEquals(0, dao.count())
    }

    @Test
    fun emptyDatabase_getAllReturnsEmptyList() = runTest {
        assertTrue(dao.getAll().isEmpty())
    }

    @Test
    fun emptyDatabase_observeAllEmitsEmptyList() = runTest {
        val items = dao.observeAll().first()
        assertTrue(items.isEmpty())
    }

    @Test
    fun insert_returnsGeneratedId() = runTest {
        val entity = entity("A", readAt = 100)
        val id = dao.insert(entity)
        assertTrue(id > 0)
    }

    @Test
    fun getAll_ordersDescByReadAt() = runTest {
        dao.insert(entity("oldest", readAt = 100))
        dao.insert(entity("middle", readAt = 200))
        dao.insert(entity("newest", readAt = 300))

        val result = dao.getAll()
        assertEquals(3, result.size)
        assertEquals("newest", result[0].tagValue)
        assertEquals("middle", result[1].tagValue)
        assertEquals("oldest", result[2].tagValue)
    }

    @Test
    fun observeAll_ordersDescByReadAt() = runTest {
        dao.insert(entity("first", readAt = 10))
        dao.insert(entity("second", readAt = 20))

        val items = dao.observeAll().first()
        assertEquals("second", items[0].tagValue)
        assertEquals("first", items[1].tagValue)
    }

    @Test
    fun count_reflectsInserts() = runTest {
        dao.insert(entity("a", readAt = 1))
        dao.insert(entity("b", readAt = 2))
        dao.insert(entity("c", readAt = 3))
        assertEquals(3, dao.count())
    }

    @Test
    fun deleteAll_removesEverything() = runTest {
        dao.insert(entity("x", readAt = 1))
        dao.insert(entity("y", readAt = 2))
        assertEquals(2, dao.count())

        dao.deleteAll()
        assertEquals(0, dao.count())
        assertTrue(dao.getAll().isEmpty())
    }

    @Test
    fun deleteAll_onEmptyDatabase_succeeds() = runTest {
        dao.deleteAll()
        assertEquals(0, dao.count())
    }

    @Test
    fun insert_specialCharacters() = runTest {
        val entity = entity("日本語 émojis 🎉", readAt = 1)
        dao.insert(entity)
        val stored = dao.getAll().single()
        assertEquals("日本語 émojis 🎉", stored.tagValue)
    }

    @Test
    fun entityStatusConstants() {
        assertEquals("SUCCESS", TagEntity.STATUS_SUCCESS)
        assertEquals("PENDING", TagEntity.STATUS_PENDING)
        assertEquals("FAILED", TagEntity.STATUS_FAILED)
    }

    private fun entity(tag: String, readAt: Long) = TagEntity(
        tagValue = tag,
        apiResponse = "resp-$tag",
        readAt = readAt,
        syncStatus = TagEntity.STATUS_SUCCESS
    )
}
