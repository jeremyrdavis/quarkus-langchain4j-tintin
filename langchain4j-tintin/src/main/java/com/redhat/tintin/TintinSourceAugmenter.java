package com.redhat.tintin;

import dev.langchain4j.rag.content.Content;
import io.quarkiverse.langchain4j.response.AiResponseAugmenter;
import io.quarkiverse.langchain4j.response.ResponseAugmenterParams;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TintinSourceAugmenter implements AiResponseAugmenter<String> {

    @Override
    public String augment(String response, ResponseAugmenterParams params) {
        List<Content> contents = params.augmentationResult() != null
                && params.augmentationResult().contents() != null
                ? params.augmentationResult().contents()
                : List.of();

        if (contents.isEmpty()) {
            return response;
        }

        StringBuilder augmented = new StringBuilder(response);
        augmented.append("\n\n---\nSources:\n");
        for (int i = 0; i < contents.size(); i++) {
            var textSegment = contents.get(i).textSegment();
            if (textSegment == null || textSegment.text() == null) {
                continue;
            }
            String segment = textSegment.text();
            String preview = segment.length() > 100 ? segment.substring(0, 100) + "..." : segment;
            augmented.append("- Source ").append(i + 1).append(": \"").append(preview).append("\"\n");
        }
        return augmented.toString();
    }
}
