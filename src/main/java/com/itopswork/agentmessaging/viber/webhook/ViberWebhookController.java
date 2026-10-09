package com.itopswork.agentmessaging.viber.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RestController
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberWebhookController {
    private static final Logger log = LoggerFactory.getLogger(ViberWebhookController.class);
    private final ViberSignatureVerifier verifier;
    private final ViberCallbackHandler handler;
    private final ObjectMapper mapper;

    public ViberWebhookController(ViberSignatureVerifier verifier, ViberCallbackHandler handler,
                                  ObjectMapper mapper) {
        this.verifier = verifier;
        this.handler = handler;
        this.mapper = mapper;
    }

    @PostMapping(ViberWebhookSettings.PATH)
    public ResponseEntity<Void> receive(@RequestHeader(value = "X-Viber-Content-Signature", required = false)
                                        String signature, @RequestBody byte[] body) {
        if (!verifier.verify(body, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ViberCallback callback;
        try {
            callback = mapper.readValue(body, ViberCallback.class);
        } catch (JacksonException ex) {
            return ResponseEntity.badRequest().build();
        }
        if (callback == null || callback.event() == null || callback.message_token() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            handler.receive(callback);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            log.warn("Viber webhook callback {} failed: {}", callback.message_token(), ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
