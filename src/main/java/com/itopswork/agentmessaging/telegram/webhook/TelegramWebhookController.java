package com.itopswork.agentmessaging.telegram.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramWebhookController {
    private static final Logger log = LoggerFactory.getLogger(TelegramWebhookController.class);
    private static final String SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";
    private final TelegramWebhookSettings settings;
    private final TelegramUpdateHandler handler;

    public TelegramWebhookController(TelegramWebhookSettings settings, TelegramUpdateHandler handler) {
        this.settings = settings;
        this.handler = handler;
    }

    @PostMapping(TelegramWebhookSettings.PATH)
    public ResponseEntity<Void> receive(@RequestHeader(value = SECRET_HEADER, required = false) String suppliedSecret,
                                        @RequestBody TelegramUpdate update) {
        if (suppliedSecret == null || !MessageDigest.isEqual(
                settings.secret().getBytes(StandardCharsets.UTF_8),
                suppliedSecret.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (update == null || update.update_id() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            handler.receive(update);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            log.warn("Telegram webhook update {} failed: {}", update.update_id(), ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
