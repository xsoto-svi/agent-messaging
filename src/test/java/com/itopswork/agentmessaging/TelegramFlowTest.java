package com.itopswork.agentmessaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.itopswork.agentmessaging.messaging.MessagingService;
import com.itopswork.agentmessaging.messaging.ReplyGenerator;
import com.itopswork.agentmessaging.telegram.TelegramApi;
import com.itopswork.agentmessaging.telegram.TelegramConnector;
import com.itopswork.agentmessaging.telegram.webhook.TelegramUpdateHandler;
import com.itopswork.agentmessaging.telegram.webhook.TelegramWebhookController;
import com.itopswork.agentmessaging.telegram.webhook.TelegramWebhookSettings;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TelegramFlowTest {
    private final TelegramApi api = mock(TelegramApi.class);
    private final ReplyGenerator generator = mock(ReplyGenerator.class);
    private final TelegramWebhookSettings settings = new TelegramWebhookSettings(
            "https://example.test/telegram/webhook", "test-secret");
    private final TelegramUpdateHandler handler = new TelegramUpdateHandler(api,
            new MessagingService(generator, new TelegramConnector(api)));
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new TelegramWebhookController(settings, handler)).build();

    @Test
    void incomingTextReachesReplyGeneratorAndReturnsToItsChat() throws Exception {
        when(generator.generate("hello")).thenReturn("AI reply");

        mvc.perform(post(TelegramWebhookSettings.PATH)
                        .header("X-Telegram-Bot-Api-Secret-Token", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"update_id":42,"message":{"message_id":1,"chat":{"id":12345},
                                 "from":{"id":99},"text":"hello"}}
                                """))
                .andExpect(status().isOk());

        verify(generator).generate("hello");
        verify(api).sendMessage("12345", "AI reply");
    }

    @Test
    void rejectsRequestWithoutMatchingSecret() throws Exception {
        mvc.perform(post(TelegramWebhookSettings.PATH)
                        .header("X-Telegram-Bot-Api-Secret-Token", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"update_id\":42}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(api, generator);
    }

    @Test
    void rejectsRequestWithoutSecret() throws Exception {
        mvc.perform(post(TelegramWebhookSettings.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"update_id\":42}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(api, generator);
    }

    @Test
    void rejectsUpdateWithoutId() throws Exception {
        mvc.perform(post(TelegramWebhookSettings.PATH)
                        .header("X-Telegram-Bot-Api-Secret-Token", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":{\"chat\":{\"id\":12345},\"text\":\"hello\"}}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(api, generator);
    }

    @Test
    void asksTelegramToRetryWhenSendingReplyFails() throws Exception {
        when(generator.generate("hello")).thenReturn("AI reply");
        doThrow(new IllegalStateException("send failed")).when(api).sendMessage("12345", "AI reply");

        mvc.perform(post(TelegramWebhookSettings.PATH)
                        .header("X-Telegram-Bot-Api-Secret-Token", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"update_id\":42,\"message\":{\"chat\":{\"id\":12345},\"text\":\"hello\"}}"))
                .andExpect(status().isServiceUnavailable());
    }
}
