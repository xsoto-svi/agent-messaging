package com.itopswork.agentmessaging.telegram;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;

class TelegramPollerTest {
    @Test
    void advancesOffsetAfterEachHandledUpdate() {
        TelegramApi api = mock(TelegramApi.class);
        TelegramUpdateHandler handler = mock(TelegramUpdateHandler.class);
        TelegramDtos.Update first = new TelegramDtos.Update(10L, null);
        TelegramDtos.Update second = new TelegramDtos.Update(11L, null);
        when(api.getUpdates(0)).thenReturn(List.of(first, second));
        when(api.getUpdates(12)).thenReturn(List.of());
        TelegramPoller poller = new TelegramPoller(api, handler);

        poller.poll();
        poller.poll();

        verify(handler).receive(first);
        verify(handler).receive(second);
        verify(api).getUpdates(12);
    }

    @Test
    void retriesFailedUpdateWithSameOffset() {
        TelegramApi api = mock(TelegramApi.class);
        TelegramUpdateHandler handler = mock(TelegramUpdateHandler.class);
        TelegramDtos.Update update = new TelegramDtos.Update(10L, null);
        when(api.getUpdates(0)).thenReturn(List.of(update));
        doThrow(new IllegalStateException("send failed")).when(handler).receive(update);
        TelegramPoller poller = new TelegramPoller(api, handler);

        poller.poll();
        poller.poll();

        verify(api, org.mockito.Mockito.times(2)).getUpdates(0);
    }
}
