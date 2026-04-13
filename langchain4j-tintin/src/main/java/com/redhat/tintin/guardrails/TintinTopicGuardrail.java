package com.redhat.tintin.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TintinTopicGuardrail implements InputGuardrail {

    private static final List<String> OFF_TOPIC_KEYWORDS = List.of(
            "stock price", "recipe", "weather forecast",
            "cryptocurrency", "medical advice", "bitcoin",
            "cooking", "investment"
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText().toLowerCase();
        for (String keyword : OFF_TOPIC_KEYWORDS) {
            if (text.contains(keyword)) {
                return failure(
                        "Billions of blue blistering barnacles! " +
                        "I can only discuss Tintin and his adventures. " +
                        "Please ask something related to the world of Tintin."
                );
            }
        }
        return success();
    }
}
