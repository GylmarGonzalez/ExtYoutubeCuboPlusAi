package com.cuboplus.ytdownloader.bot;

import com.cuboplus.ytdownloader.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Component
@EnableAsync(proxyTargetClass = true)
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Autowired
    private DownloadService downloadService;

    public TelegramBot(@Value("${telegram.bot.token}") String token) {
        super(token);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String chatId  = update.getMessage().getChatId().toString();
        String text    = update.getMessage().getText().trim();
        String name    = update.getMessage().getFrom().getFirstName();

        // Comandos
        if (text.equals("/start")) {
            send(chatId, "👋 Hola " + name + "!\n\n" +
                "Soy tu bot descargador de YouTube.\n\n" +
                "📎 Envíame una URL de YouTube y te descargo el video.\n" +
                "Ejemplo:\n`https://www.youtube.com/watch?v=dQw4w9WgXcQ`\n\n" +
                "Comandos:\n" +
                "/mp4 — descargar como video (por defecto)\n" +
                "/mp3 — descargar como audio");
            return;
        }

        // Detectar si es URL de YouTube
        if (text.contains("youtube.com/watch") || text.contains("youtu.be/")) {
            handleDownload(chatId, text, "mp4");
            return;
        }

        // Comandos con URL: /mp3 https://...
        if (text.startsWith("/mp3 ")) {
            String url = text.substring(5).trim();
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                handleDownload(chatId, url, "mp3");
                return;
            }
        }

        if (text.startsWith("/mp4 ")) {
            String url = text.substring(5).trim();
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                handleDownload(chatId, url, "mp4");
                return;
            }
        }

        send(chatId, "⚠️ Envíame una URL de YouTube válida.\nEjemplo:\n`https://www.youtube.com/watch?v=xxxxx`");
    }

    @Async
    private void handleDownload(String chatId, String url, String format) {
        try {
            // Obtener título
            String title = downloadService.getVideoTitle(url);
            send(chatId, "⏳ Descargando *" + escapeMarkdown(title) + "* en " + format.toUpperCase() + "...\nEspera un momento.");

            // Descargar
            String filename = downloadService.downloadVideo(url, format);
            File file = new File(downloadService.getDownloadDir() + "/" + filename);

            if (!file.exists()) {
                send(chatId, "❌ Error: no se encontró el archivo descargado.");
                return;
            }

            // Enviar archivo por Telegram
            send(chatId, "✅ ¡Listo! Enviando archivo...");
            SendDocument doc = new SendDocument();
            doc.setChatId(chatId);
            doc.setDocument(new InputFile(file, filename));
            doc.setCaption("🎬 " + title);
            execute(doc);

        } catch (TelegramApiException e) {
            send(chatId, "❌ Error enviando el archivo: " + e.getMessage());
        } catch (Exception e) {
            send(chatId, "❌ Error descargando: " + e.getMessage());
        }
    }

    private void send(String chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("Markdown");
        try { execute(msg); } catch (TelegramApiException e) { e.printStackTrace(); }
    }

    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_").replace("*", "\\*").replace("[", "\\[").replace("`", "\\`");
    }
}
