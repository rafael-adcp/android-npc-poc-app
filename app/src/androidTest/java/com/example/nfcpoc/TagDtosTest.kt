package com.example.nfcpoc

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nfcpoc.data.remote.dto.TagRequest
import com.example.nfcpoc.data.remote.dto.TagResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagDtosTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // --- TagRequest ---

    @Test
    fun tagRequest_serializesToJson() {
        val request = TagRequest(tagValue = "AABB", readAt = 1000L)
        val str = json.encodeToString(request)
        assertTrue(str.contains("\"tagValue\":\"AABB\""))
        assertTrue(str.contains("\"readAt\":1000"))
    }

    @Test
    fun tagRequest_deserializesFromJson() {
        val str = """{"tagValue":"CC","readAt":99}"""
        val req = json.decodeFromString<TagRequest>(str)
        assertEquals("CC", req.tagValue)
        assertEquals(99L, req.readAt)
    }

    @Test
    fun tagRequest_emptyTagValue() {
        val request = TagRequest(tagValue = "", readAt = 0L)
        val str = json.encodeToString(request)
        val decoded = json.decodeFromString<TagRequest>(str)
        assertEquals("", decoded.tagValue)
    }

    // --- TagResponse ---

    @Test
    fun tagResponse_allFieldsPresent() {
        val str = """{"id":"abc","processedAt":"2026-01-01","data":"ok"}"""
        val resp = json.decodeFromString<TagResponse>(str)
        assertEquals("abc", resp.id)
        assertEquals("2026-01-01", resp.processedAt)
        assertEquals("ok", resp.data)
    }

    @Test
    fun tagResponse_allFieldsNull() {
        val str = """{"id":null,"processedAt":null,"data":null}"""
        val resp = json.decodeFromString<TagResponse>(str)
        assertNull(resp.id)
        assertNull(resp.processedAt)
        assertNull(resp.data)
    }

    @Test
    fun tagResponse_missingFields_usesDefaults() {
        val str = """{}"""
        val resp = json.decodeFromString<TagResponse>(str)
        assertNull(resp.id)
        assertNull(resp.processedAt)
        assertNull(resp.data)
    }

    @Test
    fun tagResponse_unknownFields_ignored() {
        val str = """{"id":"x","extra":"ignored","nested":{"a":1}}"""
        val resp = json.decodeFromString<TagResponse>(str)
        assertEquals("x", resp.id)
    }

    @Test
    fun tagResponse_dataClassEquality() {
        val a = TagResponse(id = "1", processedAt = "t", data = "d")
        val b = TagResponse(id = "1", processedAt = "t", data = "d")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun tagResponse_copy() {
        val original = TagResponse(id = "1", data = "old")
        val copied = original.copy(data = "new")
        assertEquals("1", copied.id)
        assertEquals("new", copied.data)
    }

    @Test
    fun tagResponse_serializesToJson() {
        val resp = TagResponse(id = "z", processedAt = null, data = "ok")
        val str = json.encodeToString(resp)
        assertTrue(str.contains("\"id\":\"z\""))
        assertTrue(str.contains("\"data\":\"ok\""))
    }
}
