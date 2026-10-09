package com.itopswork.agentmessaging.telegram;

import com.itopswork.agentmessaging.messaging.PlatformMessageSender;
import com.itopswork.agentmessaging.messaging.OutgoingMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramConnector implements PlatformMessageSender {
    private final TelegramApi api;

    public TelegramConnector(TelegramApi api) {
        this.api = api;
    }

    @Override
    public String platform() {
        return "telegram";
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
