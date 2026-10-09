package com.itopswork.agentmessaging.messaging;

import org.springframework.stereotype.Service;

@Service
public class MessagingService {
    private final ReplyGenerator replyGenerator;
    private final MessageSender messageSender;

    public MessagingService(ReplyGenerator replyGenerator, MessageSender messageSender) {
        this.replyGenerator = replyGenerator;
        this.messageSender = messageSender;
    }

    public void process(IncomingMessage message) {
        String reply;
        try {
            reply = replyGenerator.generate(message.text());
        } catch (RuntimeException ex) {
            reply = "Sorry, I couldn't generate a response right now. Please try again later.";
        }
        messageSender.send(new OutgoingMessage(message.platform(), message.conversationId(), reply));
    }
}
