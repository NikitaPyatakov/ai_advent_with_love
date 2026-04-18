# Claude Chat — Android App

An Android chat application that lets you have conversations with Claude (Anthropic's AI) directly from your phone.

## Features

### Chat
- Real-time chat with Claude AI
- Full conversation history sent with each message so Claude has context
- **Token count** displayed under every message — estimated for user messages, exact (input/output) for assistant messages
- **Loading indicator** while waiting for a response
- Auto-scroll to the latest message after each response
- **Clear chat** button — wipes the entire conversation history with a confirmation dialog
- Error dialog with detailed API error messages
- Increased network timeouts (read: 120s) to handle long responses without errors

### Message details (long press)
Long-pressing any message opens a details dialog showing:
- Time the message was sent
- Token counts (input / output)
- For assistant messages: model used, cost of the response (USD), response time and generation speed (tokens/sec)
- **Copy** button — copies the message text to clipboard

### Settings
Configure how Claude responds via the settings dialog:
- **Model** — choose from 7 available Claude models grouped by family (Opus / Sonnet / Haiku), each with a description of its best use case
- **Answer format** — system prompt describing the desired response style (e.g. "Always respond in JSON", "Use bullet points")
- **Max answer length** — control response length via token limit (default: 1024)
- **Stop condition** — Claude stops generating as soon as it outputs this string
- **Temperature** — controls response randomness (0.0 = deterministic, 1.0 = creative)

Settings are **persisted across app restarts** (saved to SharedPreferences).

### Available models

| Model | Best for |
|---|---|
| Opus 4.6 | Deep analysis, scientific tasks |
| Opus 4.5 | Complex reasoning and strategies |
| Opus 3 | Hard tasks and programming |
| Sonnet 4.6 | Code, writing, everyday tasks |
| Sonnet 4.5 | General-purpose assistant |
| Haiku 4.5 | Fast replies, budget-conscious usage |

## Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (ViewModel + LiveData)
- **Networking:** Retrofit + OkHttp
- **UI:** ViewBinding, RecyclerView
- **Persistence:** SharedPreferences

## Setup

1. Clone the repository
2. Get an API key from [console.anthropic.com](https://console.anthropic.com)
3. Create a `local.properties` file in the project root (if it doesn't exist) and add:
   ```
   CLAUDE_API_KEY=your_api_key_here
   ```
4. Build and run the app in Android Studio

> `local.properties` is gitignored and will never be committed — your key stays local.

## Requirements

- Android 7.0+ (API 24)
- Android Studio Hedgehog or newer
