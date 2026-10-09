package com.itopswork.agentmessaging.viber;

import com.itopswork.agentmessaging.messaging.OutgoingMessage;
import com.itopswork.agentmessaging.messaging.PlatformMessageSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberConnector implements PlatformMessageSender {
    private static final int MAX_CHUNK_LENGTH = 3000;
    private final ViberApi api;

    public ViberConnector(ViberApi api) {
        this.api = api;
    }

    @Override
    public String platform() {
        return "viber";
    }

    @Override
    public void send(OutgoingMessage message) {
        if (!platform().equals(message.platform())) {
            throw new IllegalArgumentException("Unsupported platform: " + message.platform());
        }
        String text = message.text();
        for (int start = 0; start < text.length();) {
            int end = Math.min(start + MAX_CHUNK_LENGTH, text.length());
            if (end < text.length() && Character.isHighSurrogate(text.charAt(end - 1))) {
                end--;
            }
            api.sendMessage(message.conversationId(), text.substring(start, end));
            start = end;
        }
    }
}
