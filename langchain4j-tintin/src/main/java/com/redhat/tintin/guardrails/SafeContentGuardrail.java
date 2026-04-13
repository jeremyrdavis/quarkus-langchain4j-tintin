package com.redhat.tintin.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SafeContentGuardrail implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String text = responseFromLLM.text();
        if (text == null || text.isBlank()) {
            return retry("The response was empty. Please provide a substantive answer about Tintin.");
        }
        if (text.length() > 2000) {
            return retry(
                    "Your response was too long (" + text.length() + " characters). " +
                    "Please provide a more concise answer under 2000 characters."
            );
        }
        return success();
    }
}
