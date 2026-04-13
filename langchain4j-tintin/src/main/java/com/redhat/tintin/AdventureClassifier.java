package com.redhat.tintin;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface AdventureClassifier {

    @SystemMessage("""
            You are a Tintin adventure classifier. Classify adventure descriptions into categories.

            Here are examples:

            Description: "Tintin travels to a fictional South American country to investigate
            a revolution financed by an oil company."
            Category: POLITICAL_INTRIGUE
            Reasoning: "This involves political machinations and corporate manipulation of governments."
            Confidence: 90

            Description: "Tintin discovers a mysterious shooting star and races to find a meteorite
            that landed in the Arctic Ocean."
            Category: EXPLORATION
            Reasoning: "This centers on a scientific expedition and geographic exploration."
            Confidence: 85

            Description: "Tintin helps Captain Haddock reclaim his ancestral estate, Marlinspike Hall,
            and discovers hidden treasure connected to Haddock's pirate ancestor."
            Category: TREASURE_HUNT
            Reasoning: "The story revolves around finding hidden wealth and historical artifacts."
            Confidence: 95

            Now classify the following adventure description.
            """)
    @UserMessage("Description: \"{description}\"")
    ClassifiedAdventure classify(String description);
}
