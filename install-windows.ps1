# =============================================
# YT Downloader — Script de instalacion Windows
# Ejecutar como Administrador en PowerShell
# =============================================

Write-Host "🚀 Instalando dependencias para YT Downloader..." -ForegroundColor Cyan

# Verificar winget
if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    Write-Host "❌ winget no encontrado. Instala App Installer desde la Microsoft Store." -ForegroundColor Red
    exit 1
}

# Instalar Java 21
Write-Host "`n☕ Instalando Java 21..." -ForegroundColor Yellow
winget install Microsoft.OpenJDK.21 --accept-source-agreements --accept-package-agreements

# Instalar Maven
Write-Host "`n📦 Instalando Maven..." -ForegroundColor Yellow
winget install Apache.Maven --accept-source-agreements --accept-package-agreements

# Instalar yt-dlp
Write-Host "`n📹 Instalando yt-dlp..." -ForegroundColor Yellow
winget install yt-dlp --accept-source-agreements --accept-package-agreements

# Instalar ffmpeg
Write-Host "`n🎬 Instalando ffmpeg..." -ForegroundColor Yellow
winget install Gyan.FFmpeg --accept-source-agreements --accept-package-agreements

# Crear carpeta de descargas
$downloadPath = "$env:USERPROFILE\Downloads\yt-downloader"
if (-not (Test-Path $downloadPath)) {
    New-Item -ItemType Directory -Path $downloadPath | Out-Null
    Write-Host "`n📁 Carpeta de descargas creada: $downloadPath" -ForegroundColor Green
}

Write-Host "`n✅ Instalacion completada!" -ForegroundColor Green
Write-Host "📝 Recuerda actualizar application.properties:" -ForegroundColor Cyan
Write-Host "   app.download.dir=C:\\Users\\$env:USERNAME\\Downloads\\yt-downloader" -ForegroundColor White
Write-Host "`n▶  Para correr el proyecto:" -ForegroundColor Cyan
Write-Host "   mvn spring-boot:run" -ForegroundColor White
