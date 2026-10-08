package com.itopswork.agentmessaging.telegram;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
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
    public List<TelegramDtos.Update> getUpdates(long offset) {
        TelegramDtos.UpdatesResponse response = client.post().uri("/getUpdates")
                .body(Map.of("offset", offset, "timeout", 20, "allowed_updates", List.of("message")))
                .retrieve().body(TelegramDtos.UpdatesResponse.class);
        if (response == null || !response.ok() || response.result() == null) {
            throw new IllegalStateException("Invalid Telegram getUpdates response");
        }
        if (Arrays.stream(response.result()).anyMatch(update -> update == null || update.update_id() == null)) {
            throw new IllegalStateException("Telegram update is missing its ID");
        }
        return Arrays.asList(response.result());
    }

    @Override
    public void sendMessage(String chatId, String text) {
        TelegramDtos.SendResponse response = client.post().uri("/sendMessage")
                .body(Map.of("chat_id", chatId, "text", text))
                .retrieve().body(TelegramDtos.SendResponse.class);
        if (response == null || !response.ok()) {
            throw new IllegalStateException("Invalid Telegram sendMessage response");
        }
    }
}
