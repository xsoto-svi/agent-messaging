package com.itopswork.agentmessaging.messaging;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RoutingMessageSender implements MessageSender {
    private final Map<String, PlatformMessageSender> senders;

    public RoutingMessageSender(List<PlatformMessageSender> senders) {
        this.senders = senders.stream().collect(Collectors.toUnmodifiableMap(
                PlatformMessageSender::platform, Function.identity()));
    }

    @Override
    public void send(OutgoingMessage message) {
        PlatformMessageSender sender = senders.get(message.platform());
        if (sender == null) {
            throw new IllegalArgumentException("Unsupported platform: " + message.platform());
        }
        sender.send(message);
    }
}
