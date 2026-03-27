package com.cuboplus.ytdownloader.bot;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotInitializer {

    @Autowired
    private TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(telegramBot);
            System.out.println("🤖 Bot de Telegram registrado: @" + telegramBot.getBotUsername());
        } catch (TelegramApiException e) {
            System.err.println("❌ Error registrando bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
