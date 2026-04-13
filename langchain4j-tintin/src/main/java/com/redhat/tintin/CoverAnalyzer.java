package com.redhat.tintin;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.ImageUrl;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface CoverAnalyzer {

    @SystemMessage("You are an art critic and Tintin expert analyzing book covers by Hergé.")
    @UserMessage("""
            Describe this Tintin book cover in detail, including the artistic style,
            characters shown, setting, and what adventure it might depict.
            """)
    String describeCover(@ImageUrl String imageUrl);

    @SystemMessage("You are an art historian specializing in Belgian comics and the ligne claire style.")
    @UserMessage("""
            Analyze the artistic techniques and color palette used in this comic book cover.
            Discuss the use of ligne claire (clear line) technique characteristic of Hergé's work.
            """)
    String analyzeArtStyle(Image image);
}
