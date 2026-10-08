package com.itopswork.agentmessaging.telegram;

import java.util.List;

public interface TelegramApi {
    List<TelegramDtos.Update> getUpdates(long offset);

    void sendMessage(String chatId, String text);
}
