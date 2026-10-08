package com.itopswork.agentmessaging.model;

public record OutgoingMessage(String platform, String conversationId, String text) {
}
