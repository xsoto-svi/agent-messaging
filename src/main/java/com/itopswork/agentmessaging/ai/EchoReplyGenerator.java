package com.itopswork.agentmessaging.ai;

import com.itopswork.agentmessaging.messaging.ReplyGenerator;
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
