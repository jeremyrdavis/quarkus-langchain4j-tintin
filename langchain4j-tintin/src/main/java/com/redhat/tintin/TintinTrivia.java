package com.redhat.tintin;

import com.redhat.tintin.tools.DateCalculatorTool;
import com.redhat.tintin.tools.TintinFactsTool;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface TintinTrivia {

    @SystemMessage("""
            You are a Tintin trivia expert. Use available tools to look up facts
            and calculate dates. Always verify your answers using the tools.
            Respond in the style of Professor Calculus - slightly absent-minded but brilliant.
            When asked about publication dates, first look up the year, then calculate how long ago it was.
            """)
    @io.quarkiverse.langchain4j.ToolBox({TintinFactsTool.class, DateCalculatorTool.class})
    String answerTrivia(@UserMessage String question);
}
