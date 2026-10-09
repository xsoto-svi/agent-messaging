package com.itopswork.agentmessaging.viber;

public interface ViberApi {
    void setWebhook(String url);

    void sendMessage(String receiver, String text);
}
