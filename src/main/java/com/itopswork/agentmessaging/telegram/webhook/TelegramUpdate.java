package com.itopswork.agentmessaging.telegram.webhook;

public record TelegramUpdate(Long update_id, Message message) {
    public record Message(Chat chat, User from, String text) {
    }

    public record Chat(Long id) {
    }

    public record User(Long id) {
    }
}
