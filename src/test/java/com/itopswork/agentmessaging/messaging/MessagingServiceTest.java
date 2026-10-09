package com.itopswork.agentmessaging.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class MessagingServiceTest {
    @Test
    void sendsGeneratedReplyToOriginalConversation() {
        ReplyGenerator generator = mock(ReplyGenerator.class);
        MessageSender sender = mock(MessageSender.class);
        when(generator.generate("hello")).thenReturn("AI reply");
        AtomicReference<OutgoingMessage> sent = new AtomicReference<>();
        doAnswer(invocation -> { sent.set(invocation.getArgument(0)); return null; })
                .when(sender).send(any());

        new MessagingService(generator, sender).process(
                new IncomingMessage("telegram", "12345", "999", "hello"));

        verify(generator).generate("hello");
        assertEquals(new OutgoingMessage("telegram", "12345", "AI reply"), sent.get());
    }

    @Test
    void sendsFriendlyErrorWhenAiFails() {
        ReplyGenerator generator = input -> { throw new IllegalStateException("failure"); };
        MessageSender sender = mock(MessageSender.class);

        new MessagingService(generator, sender).process(
                new IncomingMessage("telegram", "12345", "999", "hello"));

        verify(sender).send(new OutgoingMessage("telegram", "12345",
                "Sorry, I couldn't generate a response right now. Please try again later."));
    }
}
