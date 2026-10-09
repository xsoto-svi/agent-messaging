package com.itopswork.agentmessaging.telegram;

public interface TelegramApi {
    void setWebhook(String url, String secret);

    void sendMessage(String chatId, String text);
}
