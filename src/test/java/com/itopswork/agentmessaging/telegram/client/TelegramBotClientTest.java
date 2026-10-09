package com.itopswork.agentmessaging.telegram.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TelegramBotClientTest {
    @Test
    void registersWebhookAndSendsReply() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org/bottest-token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.telegram.org/bottest-token/setWebhook"))
                .andExpect(content().json("""
                        {"url":"https://example.test/telegram/webhook","secret_token":"test-secret",
                         "allowed_updates":["message"],"max_connections":1}
                        """))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.telegram.org/bottest-token/sendMessage"))
                .andExpect(content().json("{\"chat_id\":\"12345\",\"text\":\"AI reply\"}"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));
        TelegramBotClient client = new TelegramBotClient(builder.build());

        client.setWebhook("https://example.test/telegram/webhook", "test-secret");
        client.sendMessage("12345", "AI reply");

        server.verify();
    }

    @Test
    void rejectsUnsuccessfulWebhookRegistration() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org/bottest-token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.telegram.org/bottest-token/setWebhook"))
                .andRespond(withSuccess("{\"ok\":false}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> new TelegramBotClient(builder.build()).setWebhook(
                        "https://example.test/telegram/webhook", "test-secret"));
        server.verify();
    }
}
