package com.redhat.tintin.chat;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;

@WebSocket(path = "/adventure-chat")
public class AdventureChatWebSocket {

    private final AdventureChatBot bot;

    public AdventureChatWebSocket(AdventureChatBot bot) {
        this.bot = bot;
    }

    @OnOpen
    public String onOpen() {
        return bot.chat("Introduce yourself briefly as Tintin and ask how you can help.");
    }

    @OnTextMessage
    public String onMessage(String message) {
        return bot.chat(message);
    }
}
