package com.itopswork.agentmessaging.viber.webhook;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberWebhookSettings {
    public static final String PATH = "/viber/webhook";

    private final String url;
    private final String token;

    public ViberWebhookSettings(@Value("${app.viber.webhook-url:}") String url,
                                @Value("${app.viber.token:}") String token) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("VIBER_WEBHOOK_URL must be an HTTPS URL ending in " + PATH);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || !PATH.equals(uri.getRawPath())
                || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("VIBER_WEBHOOK_URL must be an HTTPS URL ending in " + PATH);
        }
        if (token.isBlank()) {
            throw new IllegalArgumentException("VIBER_BOT_TOKEN is required when Viber is enabled");
        }
        this.url = url;
        this.token = token;
    }

    public String url() {
        return url;
    }

    public String token() {
        return token;
    }
}
