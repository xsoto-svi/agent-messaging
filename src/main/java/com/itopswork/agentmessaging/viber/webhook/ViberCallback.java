package com.itopswork.agentmessaging.viber.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViberCallback(String event, Long message_token, User sender, Message message) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(String id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String type, String text) {
    }
}
