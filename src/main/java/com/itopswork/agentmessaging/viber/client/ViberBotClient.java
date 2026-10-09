package com.itopswork.agentmessaging.viber.client;

import com.itopswork.agentmessaging.viber.ViberApi;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberBotClient implements ViberApi {
    private final RestClient client;
    private final String senderName;

    @Autowired
    public ViberBotClient(RestClient.Builder builder,
                          @Value("${app.viber.token}") String token,
                          @Value("${app.viber.sender-name:Agent Messaging}") String senderName,
                          @Value("${app.viber.api-base-url:https://chatapi.viber.com/pa}") String apiBaseUrl) {
        if (token.isBlank()) {
            throw new IllegalArgumentException("VIBER_BOT_TOKEN is required when Viber is enabled");
        }
        if (senderName.isBlank() || senderName.length() > 28) {
            throw new IllegalArgumentException("VIBER_SENDER_NAME must contain 1-28 characters");
        }
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.client = builder.baseUrl(apiBaseUrl)
                .defaultHeader("X-Viber-Auth-Token", token)
                .requestFactory(requestFactory)
                .build();
        this.senderName = senderName;
    }

    ViberBotClient(RestClient client, String senderName) {
        this.client = client;
        this.senderName = senderName;
    }

    @Override
    public void setWebhook(String url) {
        ViberBotResponse response = client.post().uri("/set_webhook")
                .body(Map.of("url", url, "event_types", new String[0],
                        "send_name", false, "send_photo", false))
                .retrieve().body(ViberBotResponse.class);
        requireSuccess(response, "set_webhook");
    }

    @Override
    public void sendMessage(String receiver, String text) {
        ViberBotResponse response = client.post().uri("/send_message")
                .body(Map.of("receiver", receiver, "type", "text", "text", text,
                        "sender", Map.of("name", senderName)))
                .retrieve().body(ViberBotResponse.class);
        requireSuccess(response, "send_message");
        if (response.message_token() == null) {
            throw new IllegalStateException("Viber send_message response is missing message_token");
        }
    }

    private void requireSuccess(ViberBotResponse response, String operation) {
        if (response == null || response.status() == null || response.status() != 0) {
            throw new IllegalStateException("Viber " + operation + " failed with status "
                    + (response == null ? "missing" : response.status()));
        }
    }
}
