package com.redhat.tintin.guardrails;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface GuardedTintinService {

    @SystemMessage("""
            You are a Tintin expert. Only answer questions about Tintin,
            his adventures, characters, and the world created by Hergé.
            Keep answers concise and informative.
            """)
    @InputGuardrails(TintinTopicGuardrail.class)
    @OutputGuardrails(value = SafeContentGuardrail.class, maxRetries = 3)
    String ask(@UserMessage String question);
}
