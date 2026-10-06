package com.subramanyam.lifetracker.service;

import java.util.prefs.Preferences;

/** Stores the Telegram bot details locally for the current Windows user. */
public class TelegramSettings {

    private static final String TOKEN_KEY = "telegram.botToken";
    private static final String CHAT_ID_KEY = "telegram.chatId";
    private final Preferences preferences = Preferences.userNodeForPackage(TelegramSettings.class);

    public String getBotToken() {
        return preferences.get(TOKEN_KEY, "");
    }

    public String getChatId() {
        return preferences.get(CHAT_ID_KEY, "");
    }

    public void saveBotToken(String token) {
        preferences.put(TOKEN_KEY, token.trim());
    }

    public void saveChatId(String chatId) {
        preferences.put(CHAT_ID_KEY, chatId.trim());
    }

    public boolean isConfigured() {
        return !getBotToken().isBlank() && !getChatId().isBlank();
    }
}
