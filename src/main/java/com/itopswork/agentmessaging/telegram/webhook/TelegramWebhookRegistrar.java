package com.itopswork.agentmessaging.telegram.webhook;

import com.itopswork.agentmessaging.telegram.TelegramApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramWebhookRegistrar implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookRegistrar.class);
    private final TelegramApi api;
    private final TelegramWebhookSettings settings;

    public TelegramWebhookRegistrar(TelegramApi api, TelegramWebhookSettings settings) {
        this.api = api;
        this.settings = settings;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            api.setWebhook(settings.url(), settings.secret());
            log.info("Telegram webhook registered");
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException("Telegram setWebhook failed with HTTP "
                    + ex.getStatusCode().value());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Telegram setWebhook failed: " + ex.getClass().getSimpleName());
        }
    }
}
