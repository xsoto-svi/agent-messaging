package com.itopswork.agentmessaging.viber.webhook;

import com.itopswork.agentmessaging.messaging.IncomingMessage;
import com.itopswork.agentmessaging.messaging.MessagingService;
import com.itopswork.agentmessaging.viber.ViberApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberCallbackHandler {
    private final ViberApi api;
    private final MessagingService messagingService;

    public ViberCallbackHandler(ViberApi api, MessagingService messagingService) {
        this.api = api;
        this.messagingService = messagingService;
    }

    public void receive(ViberCallback callback) {
        if (!"message".equals(callback.event()) || callback.sender() == null
                || callback.sender().id() == null || callback.sender().id().isBlank()
                || callback.message() == null) {
            return;
        }
        String userId = callback.sender().id();
        ViberCallback.Message message = callback.message();
        if (!"text".equals(message.type()) || message.text() == null || message.text().isBlank()) {
            api.sendMessage(userId, "I can reply to text messages only for now.");
        } else if (message.text().matches("(?s)^/start(?:\\s.*)?$")) {
            api.sendMessage(userId, "Hi! Send me a text message and I'll reply.");
        } else {
            messagingService.process(new IncomingMessage("viber", userId, userId, message.text()));
        }
    }
}
