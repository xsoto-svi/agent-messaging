package com.itopswork.agentmessaging.viber.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.viber.enabled", havingValue = "true")
public class ViberSignatureVerifier {
    private final SecretKeySpec key;

    public ViberSignatureVerifier(ViberWebhookSettings settings) {
        this.key = new SecretKeySpec(settings.token().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public boolean verify(byte[] body, String suppliedSignature) {
        if (suppliedSignature == null || !suppliedSignature.matches("[0-9a-fA-F]{64}")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return MessageDigest.isEqual(mac.doFinal(body), HexFormat.of().parseHex(suppliedSignature));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException ex) {
            throw new IllegalStateException("Cannot verify Viber signature", ex);
        }
    }
}
