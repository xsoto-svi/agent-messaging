package com.itopswork.agentmessaging.telegram.webhook;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TelegramWebhookSettingsTest {
    @Test
    void requiresHttpsEndpointWithExpectedPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new TelegramWebhookSettings("http://example.test/telegram/webhook", "test-secret"));
        assertThrows(IllegalArgumentException.class,
                () -> new TelegramWebhookSettings("https://example.test/wrong-path", "test-secret"));
        assertThrows(IllegalArgumentException.class,
                () -> new TelegramWebhookSettings("https://user:pass@example.test/telegram/webhook", "test-secret"));
    }

    @Test
    void requiresTelegramCompatibleSecret() {
        assertThrows(IllegalArgumentException.class,
                () -> new TelegramWebhookSettings("https://example.test/telegram/webhook", "has spaces"));
    }
}
