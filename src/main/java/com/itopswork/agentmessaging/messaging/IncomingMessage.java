package com.itopswork.agentmessaging.messaging;

public record IncomingMessage(String platform, String conversationId, String senderId, String text) {
}
