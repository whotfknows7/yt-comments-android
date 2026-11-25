# YT Comments — Android YouTube Comment Viewer

A clean, modern Android app built with **Kotlin**, **Jetpack Compose**, and **Material 3** that lets you share or paste any YouTube video and browse its comments and replies using the **official YouTube Data API v3**.

## Features

- **Share Directly from YouTube**: Appears in the Android Share Sheet (`ACTION_SEND`). Tap **Share → YT Comments** on any YouTube video, Short, or Live stream to load its comments immediately.
- **Universal URL Support**: Paste or share `youtube.com/watch?v=...`, `youtu.be/...`, `youtube.com/shorts/...`, `youtube.com/live/...`, mobile links, or a raw 11-character video ID.
- **YouTube-Style Expandable Replies**: Tap any comment (or its replies pill) to smoothly expand and load replies on demand via the official `comments.list` endpoint, with paginated **"Load more replies"** support.
- **Sort & Search**: Switch between **Top comments** (`relevance`) and **Newest first** (`time`), and filter loaded comments in real time.
- **Infinite Scroll & Pull-to-Refresh**: Smooth pagination via `nextPageToken` and Material 3 pull-to-refresh.
- **Video Header Card**: Displays the video thumbnail, title, channel name, view count, like count, and total comment count, with a shortcut to open the video in YouTube.
- **Official YouTube Data API v3 Key Support**: Configure your API key directly inside the app's **Settings** dialog (saved locally with Jetpack DataStore) or bake it in at build time via `local.properties`.
- **Recent History**: Quickly revisit previously viewed videos from the History bottom sheet.
- **Polished Material 3 UI**: Edge-to-edge layout, Material You dynamic colors (Android 12+), refined Light & Dark themes, shimmer loading skeletons, and channel-owner badges.

## Getting an Official YouTube Data API v3 Key

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create or select a project.
3. Open **APIs & Services → Library**, search for **YouTube Data API v3**, and click **Enable**.
4. Open **APIs & Services → Credentials**, click **Create Credentials → API key**.
5. Copy the key and paste it into the app's **API Key Settings** dialog (or add it to `local.properties` before building).

## Building the App

### Option 1: Download Pre-Built APK from GitHub Actions
Every push to `main` automatically runs the **Build Debug APK** workflow.
1. Open the **Actions** tab in this repository.
2. Select the latest successful workflow run.
3. Download the **`yt-comments-debug-apk`** artifact and install it on your Android device.

### Option 2: Build Locally in Android Studio
1. Clone this repository and open it in **Android Studio** (Ladybug or newer, JDK 17).
2. *(Optional)* Add your API key to `local.properties` in the project root:
   ```properties
   YOUTUBE_API_KEY=YOUR_OFFICIAL_API_KEY_HERE
   ```
   *(If omitted, you can enter your key directly inside the app's Settings screen.)*
3. Run the `app` configuration on an Android 7.0+ (API 24+) device or emulator.
