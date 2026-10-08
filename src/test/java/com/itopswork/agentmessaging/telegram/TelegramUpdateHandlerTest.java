package com.itopswork.agentmessaging.telegram;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.itopswork.agentmessaging.messaging.MessagingService;
import com.itopswork.agentmessaging.model.IncomingMessage;
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
        handler.receive(new TelegramDtos.Update(8L,
                new TelegramDtos.Message(new TelegramDtos.Chat(null), new TelegramDtos.User(99L), "hello")));
        verifyNoInteractions(api, messaging);
    }

    private TelegramDtos.Update update(long id, String text) {
        return new TelegramDtos.Update(id,
                new TelegramDtos.Message(new TelegramDtos.Chat(12345L), new TelegramDtos.User(99L), text));
    }
}
