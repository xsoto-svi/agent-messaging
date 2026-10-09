package com.itopswork.agentmessaging.openai;

import com.itopswork.agentmessaging.messaging.ReplyGenerator;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@ConditionalOnProperty(name = "app.reply-mode", havingValue = "ai")
public class OpenAiService implements ReplyGenerator {
    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);
    private final RestClient client;
    private final String model;

    @Autowired
    public OpenAiService(RestClient.Builder builder,
                         @Value("${app.openai.api-key}") String apiKey,
                         @Value("${app.openai.model}") String model) {
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("OPENAI_API_KEY is required when APP_REPLY_MODE=ai");
        }
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(45));
        this.client = builder.baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(requestFactory)
                .build();
        this.model = model;
    }

    OpenAiService(RestClient client, String model) {
        this.client = client;
        this.model = model;
    }

    @Override
    public String generate(String input) {
        try {
            OpenAiResponse response = client.post().uri("/responses")
                    .body(Map.of("model", model, "input", input, "store", false, "max_output_tokens", 512))
                    .retrieve().body(OpenAiResponse.class);
            if (response == null || !"completed".equals(response.status()) || response.output() == null) {
                throw new IllegalStateException("OpenAI returned an incomplete response");
            }
            String text = response.output().stream()
                    .filter(item -> "message".equals(item.type()) && item.content() != null)
                    .flatMap(item -> item.content().stream())
                    .filter(content -> "output_text".equals(content.type()) && content.text() != null)
                    .map(Content::text)
                    .reduce("", String::concat).trim();
            if (text.isEmpty()) {
                throw new IllegalStateException("OpenAI returned no text");
            }
            return text;
        } catch (RestClientResponseException ex) {
            String code = errorCode(ex);
            log.warn("OpenAI request failed: status={} code={}", ex.getStatusCode().value(), code);
            throw new IllegalStateException("OpenAI request failed: status="
                    + ex.getStatusCode().value() + " code=" + code, ex);
        } catch (RestClientException ex) {
            log.warn("OpenAI request failed: {}", ex.getClass().getSimpleName());
            throw new IllegalStateException("OpenAI request failed", ex);
        }
    }

    private String errorCode(RestClientResponseException ex) {
        try {
            OpenAiErrorResponse response = ex.getResponseBodyAs(OpenAiErrorResponse.class);
            if (response != null && response.error() != null) {
                String code = response.error().code();
                if (code == null) {
                    code = response.error().type();
                }
                if (code != null && code.matches("[A-Za-z0-9_-]{1,80}")) {
                    return code;
                }
            }
        } catch (RuntimeException ignored) {
            // A malformed error body must not hide the original HTTP failure.
        }
        return "unknown";
    }

    private record OpenAiErrorResponse(OpenAiError error) {
    }

    private record OpenAiError(String code, String type) {
    }

    public record OpenAiResponse(String status, List<OutputItem> output) {
    }

    public record OutputItem(String type, List<Content> content) {
    }

    public record Content(String type, String text) {
    }
}
