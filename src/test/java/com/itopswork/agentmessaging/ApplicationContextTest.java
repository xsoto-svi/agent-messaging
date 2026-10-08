package com.itopswork.agentmessaging;

import com.itopswork.agentmessaging.telegram.TelegramApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "app.reply-mode=echo",
        "app.telegram.token=test-token",
        "spring.task.scheduling.enabled=false",
        "logging.level.root=WARN"
})
class ApplicationContextTest {
    @MockitoBean
    TelegramApi telegramApi;

    @Test
    void startsWithEchoMode() {
    }
}
