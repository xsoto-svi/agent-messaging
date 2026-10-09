package com.itopswork.agentmessaging.viber.webhook;

import com.itopswork.agentmessaging.viber.ViberApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberWebhookRegistrar implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ViberWebhookRegistrar.class);
    private final ViberApi api;
    private final ViberWebhookSettings settings;

    public ViberWebhookRegistrar(ViberApi api, ViberWebhookSettings settings) {
        this.api = api;
        this.settings = settings;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            api.setWebhook(settings.url());
            log.info("Viber webhook registered");
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException("Viber set_webhook failed with HTTP " + ex.getStatusCode().value());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Viber set_webhook failed: " + ex.getClass().getSimpleName());
        }
    }
}
