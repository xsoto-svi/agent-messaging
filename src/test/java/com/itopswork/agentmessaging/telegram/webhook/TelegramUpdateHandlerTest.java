package com.itopswork.agentmessaging.telegram.webhook;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.itopswork.agentmessaging.messaging.MessagingService;
import com.itopswork.agentmessaging.messaging.IncomingMessage;
import com.itopswork.agentmessaging.telegram.TelegramApi;
import org.junit.jupiter.api.Test;

class TelegramUpdateHandlerTest {
    private final TelegramApi api = mock(TelegramApi.class);
    private final MessagingService messaging = mock(MessagingService.class);
    private final TelegramUpdateHandler handler = new TelegramUpdateHandler(api, messaging);

    @Test
    void mapsTextAndChatId() {
        handler.receive(update(5, "hello"));
        verify(messaging).process(new IncomingMessage("telegram", "12345", "99", "hello"));
    }

    @Test
    void handlesStartWithoutCallingAi() {
        handler.receive(update(6, "/start"));
        verify(api).sendMessage("12345", "Hi! Send me a text message and I'll reply.");
        verifyNoInteractions(messaging);
    }

    @Test
    void explainsUnsupportedMessage() {
        handler.receive(update(7, null));
        verify(api).sendMessage("12345", "I can reply to text messages only for now.");
        verifyNoInteractions(messaging);
    }

    @Test
    void skipsMalformedMessageWithoutChatId() {
        handler.receive(new TelegramUpdate(8L,
                new TelegramUpdate.Message(new TelegramUpdate.Chat(null), new TelegramUpdate.User(99L), "hello")));
        verifyNoInteractions(api, messaging);
    }

    private TelegramUpdate update(long id, String text) {
        return new TelegramUpdate(id,
                new TelegramUpdate.Message(new TelegramUpdate.Chat(12345L), new TelegramUpdate.User(99L), text));
    }
}
