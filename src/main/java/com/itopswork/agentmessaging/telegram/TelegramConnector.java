package com.itopswork.agentmessaging.telegram;

import com.itopswork.agentmessaging.messaging.MessageSender;
import com.itopswork.agentmessaging.model.OutgoingMessage;
import org.springframework.stereotype.Component;

@Component
public class TelegramConnector implements MessageSender {
    private final TelegramApi api;

    public TelegramConnector(TelegramApi api) {
        this.api = api;
    }

    @Override
    public void send(OutgoingMessage message) {
        if (!"telegram".equals(message.platform())) {
            throw new IllegalArgumentException("Unsupported platform: " + message.platform());
        }
        String text = message.text();
        for (int start = 0; start < text.length();) {
            int end = Math.min(start + 4000, text.length());
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) {
                end--;
            }
            api.sendMessage(message.conversationId(), text.substring(start, end));
            start = end;
        }
    }
}
