package com.itopswork.agentmessaging.model;

public record IncomingMessage(String platform, String conversationId, String senderId, String text) {
}
