package com.itopswork.agentmessaging.telegram;

public final class TelegramDtos {
    private TelegramDtos() {
    }

    public record UpdatesResponse(boolean ok, Update[] result) {
    }

    public record SendResponse(boolean ok) {
    }

    public record Update(Long update_id, Message message) {
    }

    public record Message(Chat chat, User from, String text) {
    }

    public record Chat(Long id) {
    }

    public record User(Long id) {
    }
}
