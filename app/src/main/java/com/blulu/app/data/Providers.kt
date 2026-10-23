package com.blulu.app.data

/** How a provider's HTTP API is shaped. */
enum class Protocol { GEMINI, ANTHROPIC, OPENAI_COMPAT }

data class Provider(
    val id: String,
    val name: String,
    val protocol: Protocol,
    val baseUrl: String,
    val defaultModel: String,
    val editableBaseUrl: Boolean = false,
)

object Providers {
    val all = listOf(
        Provider("gemini", "Google Gemini", Protocol.GEMINI,
            "https://generativelanguage.googleapis.com/v1beta", "gemini-2.5-flash"),
        Provider("openai", "OpenAI", Protocol.OPENAI_COMPAT,
            "https://api.openai.com/v1", "gpt-4o-mini"),
        Provider("anthropic", "Anthropic Claude", Protocol.ANTHROPIC,
            "https://api.anthropic.com/v1", "claude-sonnet-4-5"),
        Provider("openrouter", "OpenRouter", Protocol.OPENAI_COMPAT,
            "https://openrouter.ai/api/v1", "openai/gpt-4o-mini"),
        Provider("groq", "Groq", Protocol.OPENAI_COMPAT,
            "https://api.groq.com/openai/v1", "llama-3.3-70b-versatile"),
        Provider("mistral", "Mistral", Protocol.OPENAI_COMPAT,
            "https://api.mistral.ai/v1", "mistral-small-latest"),
        Provider("deepseek", "DeepSeek", Protocol.OPENAI_COMPAT,
            "https://api.deepseek.com/v1", "deepseek-chat"),
        Provider("custom", "Custom (OpenAI-compatible)", Protocol.OPENAI_COMPAT,
            "https://", "", editableBaseUrl = true),
    )

    fun byId(id: String): Provider = all.firstOrNull { it.id == id } ?: all.first()
}
