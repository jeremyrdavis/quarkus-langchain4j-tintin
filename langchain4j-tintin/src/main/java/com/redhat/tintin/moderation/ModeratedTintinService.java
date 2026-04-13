package com.redhat.tintin.moderation;

import dev.langchain4j.service.Moderate;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ModeratedTintinService {

    @SystemMessage("""
            You are a family-friendly Tintin expert for young readers.
            Answer questions about Tintin's adventures in a way that is
            appropriate for children. Keep responses fun, educational, and positive.
            """)
    @Moderate
    String askForKids(@UserMessage String question);
}
