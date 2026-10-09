package com.itopswork.agentmessaging.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OpenAiServiceTest {
    @Test
    void parsesOutputTextEvenWhenItIsNotTheFirstOutputItem() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer test-key");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().json("{\"model\":\"test-model\",\"input\":\"hello\",\"store\":false}"))
                .andRespond(withSuccess("""
                        {"id":"resp_1","status":"completed","output":[
                          {"type":"reasoning"},
                          {"type":"message","role":"assistant","content":[{"type":"output_text","text":"Hello there","annotations":[]}]}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        String reply = new OpenAiService(builder.build(), "test-model").generate("hello");

        assertEquals("Hello there", reply);
        server.verify();
    }

    @Test
    void rejectsCompletedResponseWithoutText() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andRespond(withSuccess("{\"status\":\"completed\",\"output\":[]}", MediaType.APPLICATION_JSON));

        assertThrows(IllegalStateException.class,
                () -> new OpenAiService(builder.build(), "test-model").generate("hello"));
        server.verify();
    }

    @Test
    void reportsApiFailure() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andRespond(withServerError());

        assertThrows(IllegalStateException.class,
                () -> new OpenAiService(builder.build(), "test-model").generate("hello"));
        server.verify();
    }

    @Test
    void reportsQuotaCodeFrom429WithoutLoggingRawErrorBody() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {"error":{"message":"Account-specific details", "type":"insufficient_quota",
                                  "code":"credit_balance_exhausted"}}
                                """));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new OpenAiService(builder.build(), "test-model").generate("hello"));

        assertEquals("OpenAI request failed: status=429 code=credit_balance_exhausted", error.getMessage());
        server.verify();
    }

    @Test
    void fallsBackToRateLimitTypeWhenCodeIsAbsent() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.com/v1");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":{\"type\":\"rate_limit_error\"}}"));

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new OpenAiService(builder.build(), "test-model").generate("hello"));

        assertEquals("OpenAI request failed: status=429 code=rate_limit_error", error.getMessage());
        server.verify();
    }
}
