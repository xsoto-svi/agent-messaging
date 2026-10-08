package com.itopswork.agentmessaging.telegram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class TelegramBotClientTest {
    @Test
    void pollsAndSendsToTheSameChat() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.telegram.org/bottest-token");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.telegram.org/bottest-token/getUpdates"))
                .andExpect(content().json("{\"offset\":8,\"timeout\":20,\"allowed_updates\":[\"message\"]}"))
                .andRespond(withSuccess("""
                        {"ok":true,"result":[{"update_id":8,"message":{
                          "message_id":4,"chat":{"id":12345,"type":"private"},
                          "from":{"id":99,"first_name":"A"},"text":"hello"}}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.telegram.org/bottest-token/sendMessage"))
                .andExpect(content().json("{\"chat_id\":\"12345\",\"text\":\"AI reply\"}"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));
        TelegramBotClient client = new TelegramBotClient(builder.build());

        List<TelegramDtos.Update> updates = client.getUpdates(8);
        client.sendMessage(Long.toString(updates.get(0).message().chat().id()), "AI reply");

        assertEquals(8, updates.get(0).update_id());
        assertEquals("hello", updates.get(0).message().text());
        server.verify();
    }
}
