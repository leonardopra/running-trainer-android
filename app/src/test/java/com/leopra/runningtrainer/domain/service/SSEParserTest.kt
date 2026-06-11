package com.leopra.runningtrainer.domain.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SSEParserTest {

    private val client = ClaudeHttpClient()

    @Test
    fun `parseSSEChunk extracts delta text`() {
        val line = """data: {"type":"content_block_delta","delta":{"type":"text_delta","text":"ciao"}}"""
        assertEquals("ciao", client.parseSSEChunk(line))
    }

    @Test
    fun `parseSSEChunk returns null for DONE`() {
        assertNull(client.parseSSEChunk("data: [DONE]"))
    }

    @Test
    fun `parseSSEChunk returns null for blank line`() {
        assertNull(client.parseSSEChunk(""))
    }

    @Test
    fun `parseSSEChunk returns null for non-data line`() {
        assertNull(client.parseSSEChunk("event: message_stop"))
    }

    @Test
    fun `parseSSEChunk returns null for delta without text`() {
        val line = """data: {"type":"message_start","delta":{}}"""
        assertNull(client.parseSSEChunk(line))
    }
}
