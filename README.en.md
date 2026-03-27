# 🎬 YT Downloader — cubo plus AI

A complete system to download YouTube videos through a **Chrome Extension** and a **Telegram Bot**, powered by a **Spring Boot** backend.

---

## 🏗️ System Architecture

```
[Chrome Extension]  ──POST /api/download──▶ [Spring Boot :8090]
[Telegram Bot]      ──URL message──────────▶ [Spring Boot :8090]
                                                    ↓
                                            [yt-dlp + ffmpeg]
                                                    ↓
                                         downloads/*.mp4 / *.mp3
                                                    ↓
[Chrome]  ◀── download link ─────────────────────────
[Telegram]◀── file attachment ───────────────────────
```

---

## ☕ Backend — Spring Boot

**Language:** Java 21  
**Framework:** Spring Boot 3.2  
**Port:** 8090

### Project Structure

```
src/main/java/com/cuboplus/ytdownloader/
├── YtDownloaderApplication.java  ← Entry point
├── controller/
│   └── DownloadController.java   ← Handles HTTP requests
├── service/
│   └── DownloadService.java      ← Business logic (calls yt-dlp)
├── model/
│   ├── DownloadRequest.java      ← Input model (url, format)
│   └── DownloadResponse.java     ← Response model
└── bot/
    ├── TelegramBot.java          ← Bot logic
    └── BotInitializer.java       ← Registers the bot on startup
```

### Available Endpoints

| Method | Route | Description |
|--------|-------|-------------|
| `POST` | `/api/download` | Receives URL and downloads the video |
| `GET`  | `/api/info?url=` | Gets the video title without downloading |
| `GET`  | `/api/file/{filename}` | Serves the downloaded file |
| `GET`  | `/api/health` | Checks that the server is alive |

### How `ProcessBuilder` works

The backend doesn't download videos directly — it calls **yt-dlp** as an internal terminal process:

```java
List<String> cmd = new ArrayList<>();
cmd.add("/opt/homebrew/bin/yt-dlp");
cmd.add("-o");
cmd.add("downloads/%(title)s.%(ext)s");
cmd.add(url);

ProcessBuilder pb = new ProcessBuilder(cmd);
Process process = pb.start();
process.waitFor(); // waits until done
```

This is equivalent to typing in the terminal:
```bash
yt-dlp -o "downloads/%(title)s.%(ext)s" https://youtube.com/watch?v=xxx
```

**Why ProcessBuilder instead of a Java YouTube library?**
- YouTube constantly changes its API — libraries go stale fast
- yt-dlp updates weekly and always works
- Less code, more reliable

---

## 🔌 Chrome Extension

**Technology:** Vanilla JavaScript + HTML/CSS (Manifest V3)

```
chrome-extension/
├── manifest.json   ← Permissions and configuration
├── popup.html      ← The popup window shown on click
├── popup.js        ← Logic (detects URL, calls backend)
└── icon.png        ← Icon in Chrome's toolbar
```

### How it works

1. Detects if you're on a YouTube video page
2. Fetches the video title from the backend
3. When you click **Download**, it sends the URL to the backend via `fetch()`
4. Shows the download link when complete

### How to install the extension

1. Open Chrome → `chrome://extensions`
2. Enable **"Developer mode"** (toggle in the top right)
3. Click **"Load unpacked"**
4. Select the `chrome-extension/` folder

> **Note:** The extension requires the backend running at `localhost:8090`

---

## 🤖 Telegram Bot

**Library:** telegrambots 6.9.7.1  
**Method:** Long Polling

### Bot flow

```
You send a YouTube URL in Telegram
       ↓
Telegram API → Long Polling → TelegramBot.java
       ↓
onUpdateReceived() detects the URL
       ↓
DownloadService calls yt-dlp
       ↓
yt-dlp downloads + ffmpeg merges
       ↓
Bot sends the file back to your chat
```

### Available commands

| Command | Description |
|---------|-------------|
| `/start` | Welcome message |
| `/mp4 [url]` | Download video as MP4 |
| `/mp3 [url]` | Download audio as MP3 |
| Direct URL | Auto-downloaded as MP4 |

---

## 🛠️ Prerequisites

- **Java 21** — `brew install openjdk@21`
- **Maven** — `brew install maven`
- **yt-dlp** — `brew install yt-dlp`
- **ffmpeg** — `brew install ffmpeg`

---

## 🚀 How to run the project

```bash
# Clone the repository
git clone https://github.com/GylmarGonzalez/ExtYoutubeCuboPlusAi.git
cd ExtYoutubeCuboPlusAi

# Configure variables in application.properties
# - telegram.bot.token=YOUR_TOKEN
# - telegram.bot.username=YourBotUsername
# - app.download.dir=/path/to/save/videos

# Run the backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
mvn spring-boot:run
```

Server starts at `http://localhost:8090`

---

## 📁 Where are videos saved?

By default in:
```
downloads/
```
Configurable in `application.properties` → `app.download.dir`

---

## 🪟 Does it work on Windows?

Yes, with minimal changes. `ProcessBuilder` works the same way — only paths change:

```java
boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
String ytdlp = isWindows ? "yt-dlp.exe" : "/opt/homebrew/bin/yt-dlp";
```

Install on Windows with:
```powershell
winget install yt-dlp
winget install ffmpeg
```

---

## 👨‍💻 Author

Built with **Ditto AI** 🎯 for cubo plus  
2026
