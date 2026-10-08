package com.itopswork.agentmessaging.messaging;

import com.itopswork.agentmessaging.model.OutgoingMessage;

public interface MessageSender {
    void send(OutgoingMessage message);
}
