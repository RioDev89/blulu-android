package com.blulu.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class ChatMessage(val role: String, val content: String) // role: "user" or "assistant"

class ApiException(message: String) : Exception(message)

/**
 * One entry point for every provider. Each protocol is a small function that
 * builds the request body and reads the reply. Keys are sent in headers only.
 */
object ApiClient {

    fun complete(
        provider: Provider,
        apiKey: String,
        model: String,
        baseUrl: String,
        system: String,
        history: List<ChatMessage>,
    ): String {
        require(apiKey.isNotBlank()) { "Add an API key for ${provider.name} in Settings." }
        require(model.isNotBlank()) { "Choose a model for ${provider.name} in Settings." }
        val base = baseUrl.trim().trimEnd('/')
        require(base.startsWith("https://")) { "The base URL must start with https://" }

        return when (provider.protocol) {
            Protocol.GEMINI -> gemini(base, apiKey, model, system, history)
            Protocol.ANTHROPIC -> anthropic(base, apiKey, model, system, history)
            Protocol.OPENAI_COMPAT -> openAiCompatible(base, apiKey, model, system, history)
        }
    }

    private fun gemini(
        base: String, key: String, model: String, system: String, history: List<ChatMessage>,
    ): String {
        val contents = JSONArray()
        for (m in history) {
            contents.put(
                JSONObject()
                    .put("role", if (m.role == "assistant") "model" else "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", m.content)))
            )
        }
        val body = JSONObject().put("contents", contents)
        if (system.isNotBlank()) {
            body.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system)))
            )
        }
        val json = post("$base/models/$model:generateContent", mapOf("x-goog-api-key" to key), body)
        val parts = json.optJSONArray("candidates")?.optJSONObject(0)
            ?.optJSONObject("content")?.optJSONArray("parts")
            ?: throw ApiException("No reply from Gemini (the request may have been blocked).")
        return (0 until parts.length()).joinToString("") { parts.getJSONObject(it).optString("text") }
    }

    private fun anthropic(
        base: String, key: String, model: String, system: String, history: List<ChatMessage>,
    ): String {
        val messages = JSONArray()
        for (m in history) messages.put(JSONObject().put("role", m.role).put("content", m.content))
        val body = JSONObject()
            .put("model", model)
            .put("max_tokens", 2048)
            .put("messages", messages)
        if (system.isNotBlank()) body.put("system", system)
        val json = post(
            "$base/messages",
            mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"),
            body,
        )
        val blocks = json.optJSONArray("content") ?: throw ApiException("No reply from Anthropic.")
        return (0 until blocks.length())
            .map { blocks.getJSONObject(it) }
            .filter { it.optString("type") == "text" }
            .joinToString("") { it.optString("text") }
    }

    /** Covers OpenAI, OpenRouter, Groq, Mistral, DeepSeek and any compatible server. */
    private fun openAiCompatible(
        base: String, key: String, model: String, system: String, history: List<ChatMessage>,
    ): String {
        val messages = JSONArray()
        if (system.isNotBlank()) {
            messages.put(JSONObject().put("role", "system").put("content", system))
        }
        for (m in history) messages.put(JSONObject().put("role", m.role).put("content", m.content))
        val body = JSONObject().put("model", model).put("messages", messages)
        val json = post("$base/chat/completions", mapOf("Authorization" to "Bearer $key"), body)
        return json.optJSONArray("choices")?.optJSONObject(0)
            ?.optJSONObject("message")?.optString("content")
            ?.takeIf { it.isNotEmpty() }
            ?: throw ApiException("No reply from the provider.")
    }

    private fun post(url: String, headers: Map<String, String>, body: JSONObject): JSONObject {
        val conn = URL(url).openConnection() as HttpsURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20_000
            conn.readTimeout = 120_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> conn.setRequestProperty(k, v) }
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw ApiException("HTTP $code: ${errorMessage(text)}")
            return JSONObject(text)
        } finally {
            conn.disconnect()
        }
    }

    private fun errorMessage(text: String): String = try {
        val o = JSONObject(text)
        o.optJSONObject("error")?.optString("message")?.takeIf { it.isNotEmpty() }
            ?: o.optString("message").takeIf { it.isNotEmpty() }
            ?: text.take(300)
    } catch (e: Exception) {
        text.take(300)
    }
}
