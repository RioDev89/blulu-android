package com.blulu.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * API keys live in EncryptedSharedPreferences (AES-256, master key in the
 * Android Keystore). Non-secret settings use ordinary preferences.
 */
class SettingsStore(context: Context) {

    private val secure: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "blulu_secure",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private val plain: SharedPreferences =
        context.getSharedPreferences("blulu_prefs", Context.MODE_PRIVATE)

    fun activeProviderId(): String = plain.getString("active", "gemini") ?: "gemini"

    fun setActiveProviderId(id: String) {
        plain.edit().putString("active", id).apply()
    }

    fun instructions(): String = plain.getString("instructions", "") ?: ""

    fun setInstructions(text: String) {
        plain.edit().putString("instructions", text).apply()
    }

    fun apiKey(id: String): String = secure.getString("key_$id", "") ?: ""

    fun model(id: String): String =
        secure.getString("model_$id", null) ?: Providers.byId(id).defaultModel

    fun baseUrl(id: String): String =
        secure.getString("url_$id", null) ?: Providers.byId(id).baseUrl

    fun save(id: String, key: String, model: String, baseUrl: String) {
        secure.edit()
            .putString("key_$id", key.trim())
            .putString("model_$id", model.trim())
            .putString("url_$id", baseUrl.trim())
            .apply()
    }
}
