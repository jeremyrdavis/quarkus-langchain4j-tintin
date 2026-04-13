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
                ? params.augmentationResult().contents()
                : List.of();

        if (contents.isEmpty()) {
            return response;
        }

        StringBuilder augmented = new StringBuilder(response);
        augmented.append("\n\n---\n**Sources:**\n");
        for (int i = 0; i < contents.size(); i++) {
            String segment = contents.get(i).textSegment().text();
            String preview = segment.length() > 100 ? segment.substring(0, 100) + "..." : segment;
            augmented.append("- Source ").append(i + 1).append(": \"").append(preview).append("\"\n");
        }
        return augmented.toString();
    }
}
