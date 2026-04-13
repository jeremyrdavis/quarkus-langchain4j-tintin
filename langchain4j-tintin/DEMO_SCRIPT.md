# Tintin AI Demo -- Presentation Script

A step-by-step walkthrough for demonstrating the Quarkus Langchain4j extension using the Tintin-themed demo application. Each section introduces a Quarkus Langchain4j feature, explains what it does, and provides a live example to run.

**Estimated time:** 30--45 minutes (adjust by skipping or expanding sections)

---

## Setup

Before the demo, make sure the application is running:

```bash
export OPENAI_API_KEY=your-key-here
cd langchain4j-tintin
mvn quarkus:dev
```

Open two terminals: one running the application, one for `curl` commands.

Open a browser tab to [http://localhost:8080](http://localhost:8080) to show the landing page.

---

## Act 1: The Basics -- AI Services and Structured Output

### Scene 1: Introduction

> *"Today we're going to explore Quarkus Langchain4j -- the Quarkus extension that brings LLM integration directly into your CDI-powered Java applications. Since we're in Belgium, we're going to theme everything around Belgium's most famous reporter: Tintin."*
>
> *"The core abstraction in Quarkus Langchain4j is the AI Service. You define a Java interface, annotate it, and Quarkus generates the implementation at build time. No boilerplate, no manual HTTP calls to OpenAI -- just a clean, type-safe interface."*

Show `CharacterAnalyzer.java` in the IDE:

```java
@RegisterAiService
public interface CharacterAnalyzer {

    @SystemMessage("You are an expert on Tintin characters created by Hergé...")
    @UserMessage("""
            Analyze the Tintin character named "{name}".
            Provide their full name, a detailed description, their first appearance,
            notable personality traits, and a list of adventures they appear in.
            """)
    CharacterInfo analyzeCharacter(String name);
}
```

Point out:

- `@RegisterAiService` -- tells Quarkus to generate the implementation as a CDI bean
- `@SystemMessage` -- sets the persona/instructions for the LLM
- `@UserMessage` -- the user prompt with `{name}` as a Qute template variable
- The return type is a **Java record** (`CharacterInfo`) -- Quarkus automatically injects the JSON schema into the prompt and deserializes the response

Show `CharacterInfo.java`:

```java
public record CharacterInfo(
    String name, String description, String firstAppearance,
    String notableTraits, List<String> associatedAdventures
) {}
```

### Scene 2: Live Demo -- Structured Output

> *"Let's ask it about Captain Haddock."*

```bash
curl -s -X POST http://localhost:8080/api/tintin/character \
  -H 'Content-Type: application/json' \
  -d '{"name":"Captain Haddock"}' | jq
```

**What to highlight in the response:**
- The response is proper JSON, not free-form text
- Every field in the `CharacterInfo` record is populated
- The `associatedAdventures` field is a real JSON array
- This is **structured output** -- the LLM's response is automatically deserialized into a Java record

> *"No JSON parsing code, no prompt engineering to coax JSON out of the model. Quarkus injects the schema, and the response comes back as a type-safe Java object."*

Try a second character:

```bash
curl -s -X POST http://localhost:8080/api/tintin/character \
  -H 'Content-Type: application/json' \
  -d '{"name":"Professor Calculus"}' | jq
```

---

## Act 2: RAG -- Retrieval-Augmented Generation

### Scene 3: EasyRAG Setup

> *"Now let's talk about RAG. We have a PDF of 'Tintin in the Land of the Soviets' -- the very first Tintin adventure from 1930. We want our AI to answer questions based on the actual content of the book."*
>
> *"With EasyRAG, the setup is almost zero code. You add the dependency, point it at a directory, and Quarkus does the rest."*

Show `application.properties`:

```properties
quarkus.langchain4j.easy-rag.path=tintin-docs
quarkus.langchain4j.easy-rag.max-segment-size=200
quarkus.langchain4j.easy-rag.max-results=5
```

> *"That's it. At startup, Quarkus scans the `tintin-docs` directory, finds our PDF, splits it into segments, embeds them using an in-process ONNX model, stores them in an in-memory vector store, and wires up a RetrievalAugmentor. No code needed."*

Show `TintinExpert.java`:

```java
@RegisterAiService
@SessionScoped
public interface TintinExpert {
    @SystemMessage("""
            You are a world-renowned expert on "The Adventures of Tintin" by Hergé.
            Answer questions based ONLY on the provided context...
            """)
    @ResponseAugmenter(TintinSourceAugmenter.class)
    String askAboutTintin(@UserMessage String question);
}
```

Point out `@ResponseAugmenter` -- this post-processes the LLM response to append source citations.

### Scene 4: Live Demo -- RAG with Citations

```bash
curl -s -X POST http://localhost:8080/api/tintin/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"What happens at the beginning of Tintin in the Land of the Soviets?"}'
```

**What to highlight:**
- The answer draws from the actual PDF content, not the model's general knowledge
- At the bottom, the **Sources** section shows which document segments were retrieved
- The `TintinSourceAugmenter` appended these citations automatically

Try a follow-up:

```bash
curl -s -X POST http://localhost:8080/api/tintin/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"Who does Tintin encounter on his journey?"}'
```

> *"The Response Augmenter is a post-processor -- it receives the LLM response plus the RAG augmentation results, and can modify the output. Here we're appending source citations, but you could use it for any kind of enrichment."*

---

## Act 3: Tools and Function Calling

### Scene 5: Defining Tools

> *"Function calling lets the LLM interact with your application code. You annotate methods with `@Tool`, and Quarkus exposes them to the model as callable functions."*

Show `TintinFactsTool.java`:

```java
@ApplicationScoped
public class TintinFactsTool {

    @Tool("Look up the publication year of a specific Tintin adventure by title")
    public String getPublicationYear(String adventureTitle) {
        // hardcoded map of all 24 Tintin adventures
    }

    @Tool("Get a list of all Tintin album titles in order of publication")
    public List<String> getAllAdventures() { ... }

    @Tool("Look up which characters appear in a specific Tintin adventure")
    public String getCharactersInAdventure(String adventureTitle) { ... }
}
```

Show `DateCalculatorTool.java`:

```java
@ApplicationScoped
public class DateCalculatorTool {

    @Tool("Calculate how many years ago a given year was")
    public int yearsAgo(int year) {
        return Year.now().getValue() - year;
    }
}
```

Show `TintinTrivia.java` and the `@ToolBox` annotation:

```java
@RegisterAiService
public interface TintinTrivia {
    @SystemMessage("You are a Tintin trivia expert... Respond in the style of Professor Calculus...")
    @ToolBox({TintinFactsTool.class, DateCalculatorTool.class})
    String answerTrivia(@UserMessage String question);
}
```

> *"The `@ToolBox` annotation tells Quarkus which tool classes to expose to this AI service. The model's tool descriptions -- the strings inside `@Tool` -- help the LLM decide when to call each function."*

### Scene 6: Live Demo -- Tool Chaining

> *"Watch what happens when we ask a question that requires multiple tool calls."*

```bash
curl -s -X POST http://localhost:8080/api/tintin/trivia \
  -H 'Content-Type: application/json' \
  -d '{"question":"How many years ago was Tintin in the Land of the Soviets published?"}'
```

**What to highlight (check the Quarkus console logs):**
- The LLM first calls `getPublicationYear("Tintin in the Land of the Soviets")` and gets back 1930
- Then it calls `yearsAgo(1930)` to calculate the age
- This is **tool chaining** -- the LLM autonomously sequences multiple tool calls
- The response comes in Professor Calculus's absent-minded style

Try more questions:

```bash
# List all adventures
curl -s -X POST http://localhost:8080/api/tintin/trivia \
  -H 'Content-Type: application/json' \
  -d '{"question":"Can you list all the Tintin adventures in order?"}'

# Character lookup
curl -s -X POST http://localhost:8080/api/tintin/trivia \
  -H 'Content-Type: application/json' \
  -d '{"question":"Which characters appear in Red Rackhams Treasure?"}'

# Multi-step reasoning
curl -s -X POST http://localhost:8080/api/tintin/trivia \
  -H 'Content-Type: application/json' \
  -d '{"question":"How many years passed between The Blue Lotus and Destination Moon?"}'
```

> *"The tools are just CDI beans. They can access databases, call REST clients, invoke other AI services -- anything you can do in Quarkus."*

---

## Act 4: Vision -- Image Processing

### Scene 7: Analyzing the Tintin Cover

> *"GPT-4o supports vision, and Quarkus Langchain4j makes it easy to pass images to the model. We have two approaches."*

Show `CoverAnalyzer.java`:

```java
@RegisterAiService
public interface CoverAnalyzer {

    // Approach 1: pass an image URL
    String describeCover(@ImageUrl String imageUrl);

    // Approach 2: pass base64-encoded image data
    String analyzeArtStyle(Image image);
}
```

> *"`@ImageUrl` tells Quarkus to wrap the string as an image URL payload. The `Image` type is for base64-encoded image data -- useful when you load an image from disk or a database."*

### Scene 8: Live Demo -- Cover Description

```bash
# Approach 1: Image via URL (served as static resource)
curl -s -X POST http://localhost:8080/api/tintin/cover/describe
```

**What to highlight:**
- The model describes the characters, setting, colors, and art style visible on the cover
- This uses the `@ImageUrl` annotation -- Quarkus passes the URL directly to GPT-4o

```bash
# Approach 2: Image via base64 (loaded from classpath)
curl -s -X POST http://localhost:8080/api/tintin/cover/analyze
```

**What to highlight:**
- This time the image is read from the classpath, base64-encoded, and sent as an `Image` object
- The system prompt asks specifically about *ligne claire* -- Herge's distinctive clear-line art style
- Same image, different analysis approach and persona

---

## Act 5: Few-Shot Prompting

### Scene 9: Classification with Examples

> *"Few-shot prompting is a technique where you embed example input/output pairs directly in the prompt. This guides the model to produce consistent, structured results without fine-tuning."*

Show `AdventureClassifier.java` -- point out the three examples embedded in `@SystemMessage`:

> *"We give the model three examples: a political intrigue, an exploration, and a treasure hunt. Each shows the expected format: a category, reasoning, and confidence score. Then we ask it to classify a new description."*

### Scene 10: Live Demo -- Adventure Classification

```bash
# Classify a description of the first Tintin adventure
curl -s -X POST http://localhost:8080/api/tintin/classify \
  -H 'Content-Type: application/json' \
  -d '{"description":"Tintin infiltrates Soviet Russia to report on the Bolshevik regime and uncovers state propaganda."}' | jq
```

**What to highlight:**
- Structured response with `category`, `reasoning`, and `confidencePercent`
- The model follows the pattern established by the few-shot examples
- This is also structured output -- a `ClassifiedAdventure` record with an `AdventureCategory` enum

Try a different adventure:

```bash
curl -s -X POST http://localhost:8080/api/tintin/classify \
  -H 'Content-Type: application/json' \
  -d '{"description":"Tintin and Captain Haddock travel to the moon aboard a nuclear-powered rocket designed by Professor Calculus."}' | jq
```

```bash
curl -s -X POST http://localhost:8080/api/tintin/classify \
  -H 'Content-Type: application/json' \
  -d '{"description":"Tintin receives a distress signal from his friend Chang, who is lost in the Himalayas after a plane crash. He embarks on a dangerous expedition to find him."}' | jq
```

---

## Act 6: WebSocket Chat and Streaming

### Scene 11: Real-Time Chat with Tintin

> *"For real-time chat, Quarkus Langchain4j integrates directly with WebSockets. Mark your AI service `@SessionScoped`, and Quarkus automatically ties the chat memory to each WebSocket connection."*

Show `AdventureChatBot.java`:

```java
@RegisterAiService
@SessionScoped
public interface AdventureChatBot {
    @SystemMessage("You are Tintin himself, the intrepid young Belgian reporter!...")
    String chat(@UserMessage String message);

    Multi<String> chatStreaming(@UserMessage String message);
}
```

Show `AdventureChatWebSocket.java`:

```java
@WebSocket(path = "/adventure-chat")
public class AdventureChatWebSocket {
    @OnOpen
    public String onOpen() {
        return bot.chat("Introduce yourself briefly as Tintin...");
    }

    @OnTextMessage
    public String onMessage(String message) {
        return bot.chat(message);
    }
}
```

> *"No `@MemoryId` annotation needed. Quarkus uses the WebSocket connection ID as the memory key automatically. When the connection closes, the memory is cleaned up."*

### Scene 12: Live Demo -- WebSocket Chat

Open [http://localhost:8080/chat.html](http://localhost:8080/chat.html) in the browser.

**Demo conversation:**

1. Wait for Tintin's greeting (generated on `@OnOpen`)
2. Type: *"Tell me about your first adventure"*
3. Type: *"Who was with you?"* (tests memory -- the model should recall the previous topic)
4. Type: *"What was the most dangerous moment?"* (further tests conversational memory)

**What to highlight:**
- Tintin speaks in first person and stays in character
- The conversation has **memory** -- each message builds on the previous context
- The memory window keeps the last 20 messages (configurable)
- Each browser tab gets its own isolated session

### Scene 13: Live Demo -- SSE Streaming

> *"For REST clients that want token-by-token streaming, we expose the same chat bot over Server-Sent Events."*

```bash
curl -N http://localhost:8080/api/tintin/chat/stream?message=Tell+me+about+your+adventure+in+Tibet
```

**What to highlight:**
- Tokens arrive one at a time as SSE events
- The method returns `Multi<String>` -- Quarkus handles the SSE framing
- Same AI service, different transport

---

## Act 7: Safety -- Guardrails and Moderation

### Scene 14: Input and Output Guardrails

> *"Guardrails let you validate input before it reaches the LLM and validate output before it reaches the user. They're just CDI beans implementing `InputGuardrail` or `OutputGuardrail`."*

Show `TintinTopicGuardrail.java`:

```java
@ApplicationScoped
public class TintinTopicGuardrail implements InputGuardrail {
    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        // reject off-topic keywords like "stock price", "cryptocurrency"...
    }
}
```

Show `SafeContentGuardrail.java`:

```java
@ApplicationScoped
public class SafeContentGuardrail implements OutputGuardrail {
    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        // reject empty or overly long responses, with retry
    }
}
```

Show `GuardedTintinService.java`:

```java
@InputGuardrails(TintinTopicGuardrail.class)
@OutputGuardrails(value = SafeContentGuardrail.class, maxRetries = 3)
String ask(@UserMessage String question);
```

### Scene 15: Live Demo -- Guardrails in Action

**On-topic question (passes):**

```bash
curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' \
  -d '{"question":"Who is Captain Haddock?"}'
```

**Off-topic question (blocked by input guardrail):**

```bash
curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' \
  -d '{"question":"What is the stock price of AAPL?"}'
```

**What to highlight:**
- The off-topic request returns HTTP 400 with the message: *"Billions of blue blistering barnacles! I can only discuss Tintin and his adventures."*
- The request never reached the LLM -- the input guardrail rejected it
- The output guardrail runs after the LLM responds and can trigger a retry if the response is empty or too long

Try more blocked keywords:

```bash
curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' \
  -d '{"question":"Give me a recipe for Belgian waffles"}'

curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' \
  -d '{"question":"Should I invest in cryptocurrency?"}'
```

### Scene 16: Content Moderation

> *"For content moderation, Quarkus integrates with OpenAI's moderation API. Just add `@Moderate` to your method."*

```bash
curl -s -X POST http://localhost:8080/api/tintin/moderated \
  -H 'Content-Type: application/json' \
  -d '{"question":"Tell me about Snowy the dog"}'
```

**What to highlight:**
- The `@Moderate` annotation causes Quarkus to call the moderation API on the response
- If flagged, an exception is thrown before the response reaches the user
- This service uses the `omni-moderation-latest` model
- The system prompt positions this as a "family-friendly expert for young readers"

---

## Act 8: Observability and Dev UI

### Scene 17: Dev UI

> *"Quarkus Dev UI gives you a dashboard for all your AI services, right in the browser."*

Open [http://localhost:8080/q/dev-ui](http://localhost:8080/q/dev-ui) and navigate to the LangChain4j section.

**What to show:**
- **AI Services page** -- lists all 7 registered AI services and their associated tools
- **Tools page** -- shows every `@Tool`-annotated method with descriptions
- **Chat page** -- interactive chat interface for testing the model directly
- **Moderation page** -- test content against the moderation model

### Scene 18: Metrics

> *"Every AI service method is automatically instrumented with Micrometer. No code needed."*

Open [http://localhost:8080/q/metrics](http://localhost:8080/q/metrics) in the browser, or:

```bash
curl -s http://localhost:8080/q/metrics | grep langchain4j
```

**What to highlight:**
- `langchain4j_aiservices_timed` -- duration of each AI method call
- `langchain4j_aiservices_counted` -- invocation count with success/failure tags
- Metrics are tagged by service name and method name
- OpenTelemetry tracing is also active -- each AI call creates a span, and tool calls create child spans

---

## Wrap-Up

> *"Let's recap what we covered -- all with standard Quarkus patterns, just a few annotations and configuration properties:"*

| Feature | How |
|---------|-----|
| AI Services | `@RegisterAiService` + interface |
| Prompts | `@SystemMessage`, `@UserMessage` with Qute templates |
| Structured output | Return Java records -- Quarkus handles JSON schema injection |
| RAG | EasyRAG: one config property, zero code |
| Source citations | `AiResponseAugmenter` post-processor |
| Tools / function calling | `@Tool` on CDI beans, `@ToolBox` to wire them |
| Tool chaining | Automatic -- the LLM sequences calls on its own |
| Image / vision | `@ImageUrl` or `Image` type for base64 |
| Few-shot prompting | Examples embedded in `@SystemMessage` |
| WebSocket chat | `@WebSocket` + `@SessionScoped` for auto memory |
| Streaming | `Multi<String>` return type for SSE |
| Input guardrails | `InputGuardrail` CDI bean + `@InputGuardrails` |
| Output guardrails | `OutputGuardrail` CDI bean + `@OutputGuardrails` with retry |
| Content moderation | `@Moderate` annotation |
| Metrics | Automatic with `quarkus-micrometer` |
| Tracing | Automatic with `quarkus-opentelemetry` |
| Dev UI | Automatic at `/q/dev-ui` |

> *"Everything is CDI, everything is type-safe, and everything works with Quarkus dev mode and live reload. Great snakes -- that's Quarkus Langchain4j!"*

---

## Troubleshooting During the Demo

| Problem | Fix |
|---------|-----|
| `OPENAI_API_KEY` not set | Export it in the terminal running `mvn quarkus:dev` |
| Slow first response | EasyRAG ingests the PDF at startup. First call may also be slow due to model cold start. Warm up with a quick `curl` before the demo. |
| RAG returns poor results | The Tintin PDF is a comic book (image-heavy). Text extraction may be limited. This is expected and shows a real-world RAG challenge. |
| Cover analyzer fails | Ensure `tintin-image.png` is in `src/main/resources/META-INF/resources/images/`. The `@ImageUrl` approach needs the app running at `localhost:8080`. |
| WebSocket won't connect | Check that `quarkus-websockets-next` is in `pom.xml` and the app started without errors. |
| Guardrail returns 500 instead of 400 | The `GuardedResource` catches `GuardrailException`. If you see 500, check the exception type in the logs. |

## Warm-Up Checklist

Run these before the audience arrives to prime the model and verify everything works:

```bash
# 1. Structured output
curl -s -X POST http://localhost:8080/api/tintin/character \
  -H 'Content-Type: application/json' -d '{"name":"Tintin"}' | jq

# 2. RAG
curl -s -X POST http://localhost:8080/api/tintin/ask \
  -H 'Content-Type: application/json' -d '{"question":"Who is Tintin?"}'

# 3. Tools
curl -s -X POST http://localhost:8080/api/tintin/trivia \
  -H 'Content-Type: application/json' -d '{"question":"When was The Blue Lotus published?"}'

# 4. Vision
curl -s -X POST http://localhost:8080/api/tintin/cover/describe

# 5. Classification
curl -s -X POST http://localhost:8080/api/tintin/classify \
  -H 'Content-Type: application/json' \
  -d '{"description":"A mystery adventure"}'  | jq

# 6. Guardrails
curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' -d '{"question":"Hello Tintin"}'

# 7. Moderation
curl -s -X POST http://localhost:8080/api/tintin/moderated \
  -H 'Content-Type: application/json' -d '{"question":"Tell me about Snowy"}'

# 8. Open chat.html in browser
open http://localhost:8080/chat.html
```
