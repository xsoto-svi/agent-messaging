package com.itopswork.agentmessaging.telegram;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TelegramPoller {
    private static final Logger log = LoggerFactory.getLogger(TelegramPoller.class);
    private final TelegramApi api;
    private final TelegramUpdateHandler handler;
    private long offset;

    public TelegramPoller(TelegramApi api, TelegramUpdateHandler handler) {
        this.api = api;
        this.handler = handler;
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void poll() {
        try {
            List<TelegramDtos.Update> updates = new ArrayList<>(api.getUpdates(offset));
            updates.sort(Comparator.comparingLong(TelegramDtos.Update::update_id));
            for (TelegramDtos.Update update : updates) {
                if (update.update_id() >= offset) {
                    handler.receive(update);
                    offset = update.update_id() + 1;
                }
            }
        } catch (RuntimeException ex) {
            log.warn("Telegram polling failed: {}", ex.getClass().getSimpleName());
        }
    }
}
