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
import com.itopswork.agentmessaging.messaging.RoutingMessageSender;
import com.itopswork.agentmessaging.telegram.TelegramApi;
import com.itopswork.agentmessaging.telegram.TelegramConnector;
import com.itopswork.agentmessaging.viber.ViberApi;
import com.itopswork.agentmessaging.viber.ViberConnector;
import com.itopswork.agentmessaging.viber.webhook.ViberCallbackHandler;
import com.itopswork.agentmessaging.viber.webhook.ViberSignatureVerifier;
import com.itopswork.agentmessaging.viber.webhook.ViberWebhookController;
import com.itopswork.agentmessaging.viber.webhook.ViberWebhookSettings;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

class ViberFlowTest {
    private static final String TOKEN = "test-viber-token";
    private final ViberApi viberApi = mock(ViberApi.class);
    private final TelegramApi telegramApi = mock(TelegramApi.class);
    private final ReplyGenerator generator = mock(ReplyGenerator.class);
    private final ViberWebhookSettings settings = new ViberWebhookSettings(
            "https://example.test/viber/webhook", TOKEN);
    private final MessagingService messaging = new MessagingService(generator,
            new RoutingMessageSender(List.of(new TelegramConnector(telegramApi), new ViberConnector(viberApi))));
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ViberWebhookController(
            new ViberSignatureVerifier(settings), new ViberCallbackHandler(viberApi, messaging),
            new JsonMapper())).build();

    @Test
    void signedTextRoutesToViberUserAndNotTelegram() throws Exception {
        when(generator.generate("hello")).thenReturn("AI reply");
        String body = """
                {"event":"message","timestamp":1457764197627,"message_token":42,
                 "sender":{"id":"user-123","name":"Tester"},
                 "message":{"type":"text","text":"hello","tracking_data":"ignored"}}
                """;

        mvc.perform(post(ViberWebhookSettings.PATH).header("X-Viber-Content-Signature", sign(body))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        verify(generator).generate("hello");
        verify(viberApi).sendMessage("user-123", "AI reply");
        verifyNoInteractions(telegramApi);
    }

    @Test
    void rejectsMissingOrIncorrectSignature() throws Exception {
        String body = "{\"event\":\"message\",\"message_token\":42}";
        mvc.perform(post(ViberWebhookSettings.PATH).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(ViberWebhookSettings.PATH).header("X-Viber-Content-Signature", sign(body + "x"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(viberApi, generator, telegramApi);
    }

    @Test
    void acknowledgesWebhookProbeWithoutReply() throws Exception {
        String body = "{\"event\":\"webhook\",\"message_token\":42}";
        mvc.perform(post(ViberWebhookSettings.PATH).header("X-Viber-Content-Signature", sign(body))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verifyNoInteractions(viberApi, generator, telegramApi);
    }

    @Test
    void repliesToUnsupportedMessageWithoutCallingGenerator() throws Exception {
        String body = """
                {"event":"message","message_token":43,"sender":{"id":"user-123"},
                 "message":{"type":"picture","media":"https://example.test/image.jpg"}}
                """;
        mvc.perform(post(ViberWebhookSettings.PATH).header("X-Viber-Content-Signature", sign(body))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        verify(viberApi).sendMessage("user-123", "I can reply to text messages only for now.");
        verifyNoInteractions(generator, telegramApi);
    }

    @Test
    void rejectsMalformedSignedJson() throws Exception {
        String body = "{";
        mvc.perform(post(ViberWebhookSettings.PATH).header("X-Viber-Content-Signature", sign(body))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void asksViberToRetryWhenSendingFails() throws Exception {
        String body = """
                {"event":"message","message_token":42,"sender":{"id":"user-123"},
                 "message":{"type":"text","text":"hello"}}
                """;
        when(generator.generate("hello")).thenReturn("reply");
        doThrow(new IllegalStateException("failed")).when(viberApi).sendMessage("user-123", "reply");

        mvc.perform(post(ViberWebhookSettings.PATH).header("X-Viber-Content-Signature", sign(body))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isServiceUnavailable());
    }

    private String sign(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(TOKEN.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }
}
