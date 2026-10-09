package com.itopswork.agentmessaging.telegram.webhook;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramWebhookSettings {
    public static final String PATH = "/telegram/webhook";

    private final String url;
    private final String secret;

    public TelegramWebhookSettings(@Value("${app.telegram.webhook-url}") String url,
                                   @Value("${app.telegram.webhook-secret}") String secret) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("TELEGRAM_WEBHOOK_URL must be a public HTTPS URL ending in " + PATH);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || !PATH.equals(uri.getRawPath())
                || uri.getQuery() != null || uri.getFragment() != null
                || !supportedPort(uri.getPort())) {
            throw new IllegalArgumentException("TELEGRAM_WEBHOOK_URL must be a public HTTPS URL ending in " + PATH);
        }
        if (!secret.matches("[A-Za-z0-9_-]{1,256}")) {
            throw new IllegalArgumentException("TELEGRAM_WEBHOOK_SECRET must contain 1-256 letters, digits, '_' or '-'");
        }
        this.url = url;
        this.secret = secret;
    }

    public String url() {
        return url;
    }

    public String secret() {
        return secret;
    }

    private boolean supportedPort(int port) {
        return port == -1 || port == 443 || port == 80 || port == 88 || port == 8443;
    }
}
