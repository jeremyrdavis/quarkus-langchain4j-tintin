package com.redhat.tintin;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.response.ResponseAugmenter;

@RegisterAiService
public interface TintinExpert {

    @SystemMessage("""
            You are a world-renowned expert on "The Adventures of Tintin" by Hergé.
            Answer questions based ONLY on the provided context from Tintin source documents.
            If you don't know the answer from the context, say \
            "Blistering barnacles! I don't have that information in my sources."
            Always stay in character as a knowledgeable Tintin scholar.
            """)
    @ResponseAugmenter(TintinSourceAugmenter.class)
    String askAboutTintin(@UserMessage String question);
}
