package com.blulu.app.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blulu.app.data.ApiClient
import com.blulu.app.data.ChatMessage
import com.blulu.app.data.Provider
import com.blulu.app.data.Providers
import com.blulu.app.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProviderConfig(val key: String, val model: String, val baseUrl: String)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app)

    val messages = mutableStateListOf<ChatMessage>()

    var providerId by mutableStateOf(store.activeProviderId())
        private set

    var busy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    val provider: Provider
        get() = Providers.byId(providerId)

    fun instructions(): String = store.instructions()

    fun saveInstructions(text: String) = store.setInstructions(text)

    fun config(id: String) = ProviderConfig(store.apiKey(id), store.model(id), store.baseUrl(id))

    fun saveConfig(id: String, c: ProviderConfig) = store.save(id, c.key, c.model, c.baseUrl)

    fun selectProvider(id: String) {
        providerId = id
        store.setActiveProviderId(id)
    }

    fun newChat() {
        messages.clear()
        error = null
    }

    fun dismissError() {
        error = null
    }

    fun send(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || busy) return

        messages.add(ChatMessage("user", clean))
        busy = true
        error = null

        val p = provider
        val c = config(p.id)
        val system = store.instructions()
        val history = messages.toList().takeLast(40)

        viewModelScope.launch {
            try {
                val reply = withContext(Dispatchers.IO) {
                    ApiClient.complete(p, c.key, c.model, c.baseUrl, system, history)
                }
                messages.add(ChatMessage("assistant", reply))
            } catch (e: Exception) {
                error = e.message ?: "Request failed."
            } finally {
                busy = false
            }
        }
    }
}
