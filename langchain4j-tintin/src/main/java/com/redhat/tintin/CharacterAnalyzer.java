package com.redhat.tintin;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface CharacterAnalyzer {

    @SystemMessage("You are an expert on Tintin characters created by Hergé. Provide detailed character analysis.")
    @UserMessage("""
            Analyze the Tintin character named "{name}".
            Provide their full name, a detailed description, their first appearance,
            notable personality traits, and a list of adventures they appear in.
            """)
    CharacterInfo analyzeCharacter(String name);
}
