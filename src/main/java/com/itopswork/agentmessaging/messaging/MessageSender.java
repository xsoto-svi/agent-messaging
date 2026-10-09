package com.itopswork.agentmessaging.messaging;

public interface MessageSender {
    void send(OutgoingMessage message);
}
