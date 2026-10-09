package com.itopswork.agentmessaging.viber.client;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ViberBotClientTest {
    @Test
    void registersWebhookAndSendsTextUsingViberContract() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://chatapi.viber.com/pa")
                .defaultHeader("X-Viber-Auth-Token", "test-token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://chatapi.viber.com/pa/set_webhook"))
                .andExpect(header("X-Viber-Auth-Token", "test-token"))
                .andExpect(content().json("""
                        {"url":"https://example.test/viber/webhook","event_types":[],
                         "send_name":false,"send_photo":false}
                        """))
                .andRespond(withSuccess("{\"status\":0}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://chatapi.viber.com/pa/send_message"))
                .andExpect(header("X-Viber-Auth-Token", "test-token"))
                .andExpect(content().json("""
                        {"receiver":"user-123","type":"text","text":"AI reply",
                         "sender":{"name":"Agent Messaging"}}
                        """))
                .andRespond(withSuccess("{\"status\":0,\"message_token\":123}", MediaType.APPLICATION_JSON));

        ViberBotClient client = new ViberBotClient(builder.build(), "Agent Messaging");
        client.setWebhook("https://example.test/viber/webhook");
        client.sendMessage("user-123", "AI reply");
        server.verify();
    }

    @Test
    void rejectsViberFailureEvenWhenHttpSucceeds() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://chatapi.viber.com/pa");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://chatapi.viber.com/pa/send_message"))
                .andRespond(withSuccess("{\"status\":6,\"status_message\":\"Not subscribed\"}",
                        MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> new ViberBotClient(builder.build(), "Agent Messaging")
                        .sendMessage("user-123", "reply"));
        server.verify();
    }

    @Test
    void rejectsSuccessResponseWithoutMessageToken() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://chatapi.viber.com/pa");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://chatapi.viber.com/pa/send_message"))
                .andRespond(withSuccess("{\"status\":0}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> new ViberBotClient(builder.build(), "Agent Messaging")
                        .sendMessage("user-123", "reply"));
        server.verify();
    }
}
