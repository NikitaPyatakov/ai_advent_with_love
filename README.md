# Claude Chat — Android App

An Android chat application that lets you have conversations with Claude (Anthropic's AI) directly from your phone.

## Features

- Real-time chat with Claude AI (claude-sonnet-4-6)
- Full conversation history sent with each message so Claude has context
- **Settings dialog** — configure how Claude responds:
  - **Answer format** — describe the desired response style (e.g. "Always respond in valid JSON format", "Use bullet points")
  - **Max answer length** — control response length via token limit (default: 1024)
  - **Stop condition** — provide a stop sequence; Claude stops generating as soon as it outputs that string
- Settings are **persisted across app restarts** (saved to SharedPreferences)
- **Clear chat** button — wipes the entire conversation history with a confirmation dialog
- Error dialog with detailed API error messages
- Loading indicator while waiting for a response
- Increased network timeouts (read: 120s) to handle long responses without errors

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
