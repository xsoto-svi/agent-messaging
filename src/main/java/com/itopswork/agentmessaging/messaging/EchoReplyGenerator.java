package com.itopswork.agentmessaging.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.reply-mode", havingValue = "echo")
public class EchoReplyGenerator implements ReplyGenerator {
    @Override
    public String generate(String input) {
        return input;
    }
}
