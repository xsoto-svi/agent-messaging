package com.itopswork.agentmessaging.messaging;

public record OutgoingMessage(String platform, String conversationId, String text) {
}
