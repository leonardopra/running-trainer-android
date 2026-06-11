package com.leopra.runningtrainer.domain.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class ClaudeRequest(
    val prompt: String,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val maxTokens: Int = 1024
) {
    companion object {
        const val DEFAULT_SYSTEM_PROMPT =
            "You are an expert running coach. Provide concise, practical workout guidance. " +
            "Always respond with valid JSON only — no markdown, no code fences."
    }
}

class ClaudeHttpClient {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun call(apiKey: String, request: ClaudeRequest): String =
        callWithRetry(apiKey, request, attempt = 0)

    private suspend fun callWithRetry(
        apiKey: String,
        request: ClaudeRequest,
        attempt: Int
    ): String = withContext(Dispatchers.IO) {
        val body = buildJsonObject {
            put("model", MODEL)
            put("max_tokens", request.maxTokens)
            put("system", request.systemPrompt)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", request.prompt)
                })
            })
        }.toString()

        val conn = URL(API_URL).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 30_000
            conn.readTimeout = 60_000
            conn.doOutput = true
            conn.setRequestProperty("x-api-key", apiKey)
            conn.setRequestProperty("anthropic-version", "2023-06-01")
            conn.setRequestProperty("content-type", "application/json")
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body) }

            val status = conn.responseCode
            if (status == 401) throw ClaudeApiException("Invalid API key. Check your key in Settings.", isAuthError = true)
            if (status == 429) {
                if (attempt < MAX_RETRIES) {
                    delay(2_000L * (attempt + 1))
                    return@withContext callWithRetry(apiKey, request, attempt + 1)
                }
                throw ClaudeApiException("Rate limited by Claude API.")
            }
            if (status !in 200..299) throw ClaudeApiException("Claude API error: HTTP $status")

            val responseBody = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
            val parsed = json.parseToJsonElement(responseBody).jsonObject
            parsed["content"]?.jsonArray?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
                ?: throw ClaudeApiException("Unexpected response structure.")
        } finally {
            conn.disconnect()
        }
    }

    fun streamRequest(apiKey: String, request: ClaudeRequest): Flow<String> = flow {
        val body = buildJsonObject {
            put("model", MODEL)
            put("max_tokens", request.maxTokens)
            put("system", request.systemPrompt)
            put("stream", true)
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", request.prompt)
                })
            })
        }.toString()

        val conn = withContext(Dispatchers.IO) {
            (URL(API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 30_000
                readTimeout = 120_000
                doOutput = true
                setRequestProperty("x-api-key", apiKey)
                setRequestProperty("anthropic-version", "2023-06-01")
                setRequestProperty("content-type", "application/json")
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { it.write(body) }
            }
        }

        try {
            val status = withContext(Dispatchers.IO) { conn.responseCode }
            if (status == 401) throw ClaudeApiException("Invalid API key. Check your key in Settings.", isAuthError = true)
            if (status !in 200..299) throw ClaudeApiException("Claude API error: HTTP $status")

            val reader = BufferedReader(
                InputStreamReader(conn.inputStream, Charsets.UTF_8)
            )
            reader.use {
                while (true) {
                    val line = withContext(Dispatchers.IO) { it.readLine() } ?: break
                    when {
                        line.isEmpty() -> continue
                        line == "data: [DONE]" -> return@flow
                        line.startsWith("data: ") -> {
                            val text = parseSSEChunk(line)
                            if (text != null) emit(text)
                        }
                    }
                }
            }
        } finally {
            withContext(Dispatchers.IO) { conn.disconnect() }
        }
    }

    /** Parses one SSE data line and returns the text delta to emit, or null to skip. */
    internal fun parseSSEChunk(line: String): String? {
        if (line.isBlank() || !line.startsWith("data: ") || line == "data: [DONE]") return null
        val payload = line.removePrefix("data: ")
        return try {
            json.parseToJsonElement(payload).jsonObject
                .get("delta")?.jsonObject
                ?.get("text")?.jsonPrimitive?.content
                ?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val MODEL = "claude-sonnet-4-6"
        private const val MAX_RETRIES = 3
    }
}
