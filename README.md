# Blulu AI (Android)

A native Android chat app (Kotlin + Jetpack Compose) that talks directly to the AI provider you choose. It has no backend. Your API key and messages go from the phone to the provider.

## Providers

| Provider | Endpoint | Protocol |
|---|---|---|
| Google Gemini | generativelanguage.googleapis.com/v1beta | Gemini generateContent |
| OpenAI | api.openai.com/v1 | chat/completions |
| Anthropic Claude | api.anthropic.com/v1 | messages |
| OpenRouter | openrouter.ai/api/v1 | chat/completions |
| Groq | api.groq.com/openai/v1 | chat/completions |
| Mistral | api.mistral.ai/v1 | chat/completions |
| DeepSeek | api.deepseek.com/v1 | chat/completions |
| Custom | any https OpenAI-compatible URL you enter | chat/completions |

To add a provider, add one line to `Providers.all`. If it speaks the OpenAI format, nothing else is needed.

## Build

1. Install Android Studio (Ladybug or newer) with the Android 15 (API 35) SDK.
2. Open this folder. If Android Studio offers to create the Gradle wrapper, accept.
3. Run on a device or emulator (Android 8.0 / API 26 or newer), or choose Build > Build APK(s).

## Notes

- Keys are stored with `EncryptedSharedPreferences`, which uses the Android Keystore. They are never written to logs or sent anywhere except the provider's own endpoint.
- Base URLs must use https.
- The app needs an internet connection to reach a provider. Model replies can't work offline.
- Chats are kept in memory and cleared when the app closes. Custom instructions are saved.
- Model names change often. Use the Model field to set whatever your provider currently offers.
