package com.itopswork.agentmessaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import com.itopswork.agentmessaging.telegram.TelegramApi;
import com.itopswork.agentmessaging.viber.ViberApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "app.reply-mode=echo",
        "app.telegram.enabled=false",
        "app.viber.enabled=true",
        "app.viber.token=test-token",
        "app.viber.webhook-url=https://example.test/viber/webhook",
        "logging.level.root=WARN"
})
class ViberApplicationContextTest {
    @MockitoBean
    ViberApi viberApi;

    @Autowired
    ApplicationContext context;

    @Test
    void startsWithOnlyViberAndRegistersWebhook() {
        assertFalse(context.containsBean("telegramWebhookController"));
        assertTrue(context.getBeansOfType(TelegramApi.class).isEmpty());
        verify(viberApi).setWebhook("https://example.test/viber/webhook");
    }
}
