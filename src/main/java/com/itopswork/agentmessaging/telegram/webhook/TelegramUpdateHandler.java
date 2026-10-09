package com.itopswork.agentmessaging.telegram.webhook;

import com.itopswork.agentmessaging.messaging.MessagingService;
import com.itopswork.agentmessaging.messaging.IncomingMessage;
import com.itopswork.agentmessaging.telegram.TelegramApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramUpdateHandler {
    private final TelegramApi api;
    private final MessagingService messagingService;

    public TelegramUpdateHandler(TelegramApi api, MessagingService messagingService) {
        this.api = api;
        this.messagingService = messagingService;
    }

    public void receive(TelegramUpdate update) {
        TelegramUpdate.Message message = update.message();
        if (message == null || message.chat() == null || message.chat().id() == null) {
            return;
        }
        String chatId = Long.toString(message.chat().id());
        String text = message.text();
        if (text == null || text.isBlank()) {
            api.sendMessage(chatId, "I can reply to text messages only for now.");
        } else if (text.matches("(?s)^/start(?:@[A-Za-z0-9_]+)?(?:\\s.*)?$")) {
            api.sendMessage(chatId, "Hi! Send me a text message and I'll reply.");
        } else {
            String senderId = message.from() == null || message.from().id() == null
                    ? chatId : Long.toString(message.from().id());
            messagingService.process(new IncomingMessage("telegram", chatId, senderId, text));
        }
    }
}
