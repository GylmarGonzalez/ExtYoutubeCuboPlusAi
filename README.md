# 🎬 YT Downloader — cubo plus AI

Sistema completo para descargar videos de YouTube mediante una **extensión de Chrome** y un **bot de Telegram**, con un backend construido en **Spring Boot**.

---

## 🏗️ Arquitectura del sistema

```
[Chrome Extension]  ──POST /api/download──▶ [Spring Boot :8090]
[Telegram Bot]      ──URL mensaje──────────▶ [Spring Boot :8090]
                                                    ↓
                                            [yt-dlp + ffmpeg]
                                                    ↓
                                         downloads/*.mp4 / *.mp3
                                                    ↓
[Chrome]  ◀── link de descarga ──────────────────────
[Telegram]◀── archivo adjunto ───────────────────────
```

---

## ☕ Backend — Spring Boot

**Lenguaje:** Java 21  
**Framework:** Spring Boot 3.2  
**Puerto:** 8090

### Estructura del proyecto

```
src/main/java/com/cuboplus/ytdownloader/
├── YtDownloaderApplication.java  ← Punto de entrada
├── controller/
│   └── DownloadController.java   ← Recibe peticiones HTTP
├── service/
│   └── DownloadService.java      ← Lógica (llama a yt-dlp)
├── model/
│   ├── DownloadRequest.java      ← Modelo de entrada (url, format)
│   └── DownloadResponse.java     ← Modelo de respuesta
└── bot/
    ├── TelegramBot.java          ← Lógica del bot
    └── BotInitializer.java       ← Registra el bot al arrancar
```

### Endpoints disponibles

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/download` | Recibe URL y descarga el video |
| `GET`  | `/api/info?url=` | Obtiene el título sin descargar |
| `GET`  | `/api/file/{nombre}` | Sirve el archivo descargado |
| `GET`  | `/api/health` | Verifica que el servidor está vivo |

### ¿Cómo funciona `ProcessBuilder`?

El backend no descarga los videos directamente — llama a **yt-dlp** como si fuera una terminal interna:

```java
List<String> cmd = new ArrayList<>();
cmd.add("/opt/homebrew/bin/yt-dlp");
cmd.add("-o");
cmd.add("downloads/%(title)s.%(ext)s");
cmd.add(url);

ProcessBuilder pb = new ProcessBuilder(cmd);
Process process = pb.start();
process.waitFor(); // espera que termine
```

Es equivalente a escribir en la terminal:
```bash
yt-dlp -o "downloads/%(title)s.%(ext)s" https://youtube.com/watch?v=xxx
```

**¿Por qué ProcessBuilder y no una librería Java de YouTube?**
- YouTube cambia su API constantemente — las librerías se desactualizan
- yt-dlp se actualiza cada semana y siempre funciona
- Menos código, más confiable

---

## 🔌 Extensión de Chrome

**Tecnología:** JavaScript puro + HTML/CSS (Manifest V3)

```
chrome-extension/
├── manifest.json   ← Permisos y configuración
├── popup.html      ← Ventana que aparece al hacer click
├── popup.js        ← Lógica (detecta URL, llama al backend)
└── icon.png        ← Ícono en la barra de Chrome
```

### Cómo funciona

1. Detecta si estás en un video de YouTube
2. Obtiene el título del video desde el backend
3. Al hacer click en **Descargar**, envía la URL al backend via `fetch()`
4. Muestra el link de descarga cuando termina

### Instalar la extensión

1. Abre Chrome → `chrome://extensions`
2. Activa **"Modo desarrollador"** (switch arriba a la derecha)
3. Click en **"Cargar descomprimida"**
4. Selecciona la carpeta `chrome-extension/`

> **Nota:** La extensión requiere que el backend esté corriendo en `localhost:8090`

---

## 🤖 Bot de Telegram

**Librería:** telegrambots 6.9.7.1  
**Método:** Long Polling

### Flujo del bot

```
Tú envías URL en Telegram
       ↓
Telegram API → Long Polling → TelegramBot.java
       ↓
onUpdateReceived() detecta la URL
       ↓
DownloadService llama a yt-dlp
       ↓
yt-dlp descarga + ffmpeg mezcla
       ↓
Bot envía el archivo de vuelta a tu chat
```

### Comandos disponibles

| Comando | Descripción |
|---------|-------------|
| `/start` | Mensaje de bienvenida |
| `/mp4 [url]` | Descargar video en MP4 |
| `/mp3 [url]` | Descargar audio en MP3 |
| URL directa | Se descarga automáticamente en MP4 |

---

## 🛠️ Requisitos previos

- **Java 21** — `brew install openjdk@21`
- **Maven** — `brew install maven`
- **yt-dlp** — `brew install yt-dlp`
- **ffmpeg** — `brew install ffmpeg`

---

## 🚀 Cómo correr el proyecto

```bash
# Clonar el repositorio
git clone https://github.com/GylmarGonzalez/ExtYoutubeCuboPlusAi.git
cd ExtYoutubeCuboPlusAi

# Configurar variables en application.properties
# - telegram.bot.token=TU_TOKEN
# - telegram.bot.username=TuBotUsername
# - app.download.dir=/ruta/donde/guardar/videos

# Correr el backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8090`

---

## 📁 Dónde se guardan los videos

Por defecto en:
```
downloads/
```
Configurable en `application.properties` → `app.download.dir`

---

## 🪟 ¿Funciona en Windows?

Sí, con ajustes mínimos. El `ProcessBuilder` es idéntico, solo cambian las rutas:

```java
boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
String ytdlp = isWindows ? "yt-dlp.exe" : "/opt/homebrew/bin/yt-dlp";
```

En Windows instalar con:
```powershell
winget install yt-dlp
winget install ffmpeg
```

---

## 👨‍💻 Autor

Desarrollado con **Ditto AI** 🎯 para cubo plus  
2026
