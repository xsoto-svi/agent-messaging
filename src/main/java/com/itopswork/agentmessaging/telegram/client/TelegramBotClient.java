package com.itopswork.agentmessaging.telegram.client;

import com.itopswork.agentmessaging.telegram.TelegramApi;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramBotClient implements TelegramApi {
    private final RestClient client;

    @Autowired
    public TelegramBotClient(RestClient.Builder builder, @Value("${app.telegram.token}") String token) {
        if (token.isBlank()) {
            throw new IllegalArgumentException("TELEGRAM_BOT_TOKEN is required");
        }
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.client = builder.baseUrl("https://api.telegram.org/bot" + token)
                .requestFactory(requestFactory)
                .build();
    }

    TelegramBotClient(RestClient client) {
        this.client = client;
    }

    @Override
    public void setWebhook(String url, String secret) {
        TelegramBotResponse response = client.post().uri("/setWebhook")
                .body(Map.of("url", url, "secret_token", secret,
                        "allowed_updates", List.of("message"), "max_connections", 1))
                .retrieve().body(TelegramBotResponse.class);
        if (response == null || !response.ok()) {
            throw new IllegalStateException("Invalid Telegram setWebhook response");
        }
    }

    @Override
    public void sendMessage(String chatId, String text) {
        TelegramBotResponse response = client.post().uri("/sendMessage")
                .body(Map.of("chat_id", chatId, "text", text))
                .retrieve().body(TelegramBotResponse.class);
        if (response == null || !response.ok()) {
            throw new IllegalStateException("Invalid Telegram sendMessage response");
        }
    }
}
