package com.itopswork.agentmessaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.itopswork.agentmessaging.messaging.MessagingService;
import com.itopswork.agentmessaging.messaging.ReplyGenerator;
import com.itopswork.agentmessaging.telegram.TelegramApi;
import com.itopswork.agentmessaging.telegram.TelegramConnector;
import com.itopswork.agentmessaging.telegram.TelegramDtos;
import com.itopswork.agentmessaging.telegram.TelegramPoller;
import com.itopswork.agentmessaging.telegram.TelegramUpdateHandler;
import java.util.List;
import org.junit.jupiter.api.Test;

class TelegramFlowTest {
    @Test
    void incomingTextReachesAiAndReplyReturnsToItsChat() {
        TelegramApi api = mock(TelegramApi.class);
        ReplyGenerator generator = mock(ReplyGenerator.class);
        when(api.getUpdates(0)).thenReturn(List.of(new TelegramDtos.Update(42L,
                new TelegramDtos.Message(new TelegramDtos.Chat(12345L),
                        new TelegramDtos.User(99L), "hello"))));
        when(generator.generate("hello")).thenReturn("AI reply");
        MessagingService messaging = new MessagingService(generator, new TelegramConnector(api));
        TelegramPoller poller = new TelegramPoller(api, new TelegramUpdateHandler(api, messaging));

        poller.poll();

        verify(generator).generate("hello");
        verify(api).sendMessage("12345", "AI reply");
    }
}
