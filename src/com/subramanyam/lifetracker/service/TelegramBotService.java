package com.subramanyam.lifetracker.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Sends messages through the Telegram Bot API using the user's own bot token. */
public class TelegramBotService {

    private static final Pattern CHAT_ID_PATTERN = Pattern.compile(
            "\\\"chat\\\"\\s*:\\s*\\{.*?\\\"id\\\"\\s*:\\s*(-?\\d+)", Pattern.DOTALL);

    private final TelegramSettings settings;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public TelegramBotService(TelegramSettings settings) {
        this.settings = settings;
    }

    public void sendIfConfigured(String message) {
        if (!settings.isConfigured()) {
            return;
        }
        try {
            sendMessage(settings.getChatId(), message);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            System.err.println("Could not send Telegram reminder: " + exception.getMessage());
        } catch (IOException exception) {
            System.err.println("Could not send Telegram reminder: " + exception.getMessage());
        }
    }

    public void sendMessage(String chatId, String message) throws IOException, InterruptedException {
        String body = "chat_id=" + encode(chatId) + "&text=" + encode(message);
        HttpRequest request = HttpRequest.newBuilder(apiUri("sendMessage"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || !response.body().contains("\"ok\":true")) {
            throw new IOException("Telegram rejected the request. Check the token and chat ID.");
        }
    }

    /** Finds the chat ID after the user has sent /start to their bot. */
    public Optional<String> findLatestChatId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(apiUri("getUpdates"))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || !response.body().contains("\"ok\":true")) {
            throw new IOException("Telegram could not read messages. Check the bot token.");
        }
        Matcher matcher = CHAT_ID_PATTERN.matcher(response.body());
        String latestChatId = null;
        while (matcher.find()) {
            latestChatId = matcher.group(1);
        }
        return Optional.ofNullable(latestChatId);
    }

    private URI apiUri(String method) {
        return URI.create("https://api.telegram.org/bot" + settings.getBotToken() + "/" + method);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
