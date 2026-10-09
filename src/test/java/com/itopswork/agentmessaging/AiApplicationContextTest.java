package com.itopswork.agentmessaging;

import com.itopswork.agentmessaging.telegram.TelegramApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "app.reply-mode=ai",
        "app.telegram.token=test-token",
        "app.telegram.webhook-url=https://example.test/telegram/webhook",
        "app.telegram.webhook-secret=test-secret",
        "app.openai.api-key=test-key",
        "logging.level.root=WARN"
})
class AiApplicationContextTest {
    @MockitoBean
    TelegramApi telegramApi;

    @Test
    void startsWithAiMode() {
    }
}
