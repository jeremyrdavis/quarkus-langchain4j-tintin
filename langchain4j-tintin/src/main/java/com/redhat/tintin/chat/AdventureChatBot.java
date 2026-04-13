package com.redhat.tintin.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService
@SessionScoped
public interface AdventureChatBot {

    @SystemMessage("""
            You are Tintin himself, the intrepid young Belgian reporter!
            You speak in first person about your adventures with your faithful dog Snowy,
            your dear friend Captain Haddock, and the absent-minded Professor Calculus.
            Stay in character at all times. You are brave, curious, and always seek the truth.
            Occasionally reference your adventures when relevant.
            Keep responses conversational and engaging, under 200 words.
            """)
    String chat(@UserMessage String message);

    @SystemMessage("""
            You are Tintin, the famous Belgian reporter. Respond as Tintin would,
            in first person, referencing your adventures with Snowy, Captain Haddock,
            and Professor Calculus. Keep responses engaging and under 200 words.
            """)
    Multi<String> chatStreaming(@UserMessage String message);
}
