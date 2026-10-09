package com.itopswork.agentmessaging.telegram;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.itopswork.agentmessaging.messaging.OutgoingMessage;
import org.junit.jupiter.api.Test;

class TelegramConnectorTest {
    @Test
    void splitsLongRepliesWithoutBreakingEmoji() {
        TelegramApi api = mock(TelegramApi.class);
        String first = "a".repeat(3999);
        String emoji = "😀";
        new TelegramConnector(api).send(new OutgoingMessage("telegram", "12345", first + emoji + "tail"));

        verify(api).sendMessage("12345", first);
        verify(api).sendMessage("12345", emoji + "tail");
    }
}
