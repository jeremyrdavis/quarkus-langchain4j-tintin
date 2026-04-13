# Tintin AI Demo -- Quarkus Langchain4j

A comprehensive demo application showcasing the features of the [Quarkus Langchain4j](https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html) extension, themed around Herge's *The Adventures of Tintin*.

The application demonstrates AI Services, Retrieval-Augmented Generation (RAG), function calling with tools, image/vision processing, guardrails, content moderation, WebSocket chat, streaming responses, structured output, few-shot prompting, and observability -- all in a single Quarkus project.

## Prerequisites

- Java 17+
- Maven 3.9+
- An [OpenAI API key](https://platform.openai.com/api-keys) with access to GPT-4o and the moderation API

## Quick Start

```bash
export OPENAI_API_KEY=your-key-here
mvn quarkus:dev
```

Open [http://localhost:8080](http://localhost:8080) to see the landing page with an interactive "Try It Out" panel.

## Project Structure

```
langchain4j-tintin/
├── pom.xml
└── src/main/
    ├── java/com/redhat/tintin/
    │   ├── TintinExpert.java                 # RAG AI Service
    │   ├── TintinSourceAugmenter.java        # Response augmenter (source citations)
    │   ├── TintinExpertResource.java
    │   ├── CharacterAnalyzer.java            # Structured output AI Service
    │   ├── CharacterInfo.java                # Java record for structured response
    │   ├── CharacterAnalyzerResource.java
    │   ├── AdventureClassifier.java          # Few-shot prompting AI Service
    │   ├── AdventureCategory.java            # Classification enum
    │   ├── ClassifiedAdventure.java          # Structured classification result
    │   ├── AdventureClassifierResource.java
    │   ├── CoverAnalyzer.java                # Image/vision AI Service
    │   ├── CoverAnalyzerResource.java
    │   ├── TintinTrivia.java                 # Tools + function calling AI Service
    │   ├── TintinTriviaResource.java
    │   ├── QueryRequest.java                 # Shared request record
    │   ├── tools/
    │   │   ├── TintinFactsTool.java          # @Tool CDI bean (facts lookup)
    │   │   └── DateCalculatorTool.java       # @Tool CDI bean (date math)
    │   ├── chat/
    │   │   ├── AdventureChatBot.java         # @SessionScoped chat AI Service
    │   │   ├── AdventureChatWebSocket.java   # WebSocket endpoint
    │   │   └── StreamingChatResource.java    # SSE streaming endpoint
    │   ├── guardrails/
    │   │   ├── TintinTopicGuardrail.java     # Input guardrail
    │   │   ├── SafeContentGuardrail.java     # Output guardrail
    │   │   ├── GuardedTintinService.java     # AI Service with guardrails
    │   │   └── GuardedResource.java
    │   └── moderation/
    │       ├── ModeratedTintinService.java   # @Moderate AI Service
    │       └── ModerationResource.java
    └── resources/
        ├── application.properties
        ├── tintin-docs/                      # EasyRAG document directory
        │   └── tintin-in-the-land-of-the-soviets.pdf
        └── META-INF/resources/
            ├── index.html                    # Landing page
            ├── chat.html                     # WebSocket chat UI
            └── images/tintin-image.png       # Book cover for vision analysis
```

## Features

### 1. AI Services (`@RegisterAiService`)

Every AI capability is defined as a declarative Java interface annotated with `@RegisterAiService`. Quarkus generates the implementation automatically at build time. System prompts are set with `@SystemMessage` and user input is passed via `@UserMessage` with Qute template variable interpolation (e.g. `{name}`, `{description}`).

### 2. Structured Output

**CharacterAnalyzer** returns a `CharacterInfo` Java record containing `name`, `description`, `firstAppearance`, `notableTraits`, and `associatedAdventures`. Quarkus automatically injects the JSON schema into the prompt and deserializes the LLM response back into the record.

**AdventureClassifier** returns a `ClassifiedAdventure` record with an `AdventureCategory` enum, `reasoning`, and `confidencePercent`.

```bash
curl -s -X POST http://localhost:8080/api/tintin/character \
  -H 'Content-Type: application/json' \
  -d '{"name":"Captain Haddock"}' | jq
```

### 3. Retrieval-Augmented Generation (RAG)

**TintinExpert** uses **EasyRAG** to automatically ingest the Tintin PDF at startup. The extension splits the document into segments, embeds them using an in-process ONNX model (e5-small-v2), stores them in an in-memory vector store, and wires up a `RetrievalAugmentor` -- all with zero code. Only the config property `quarkus.langchain4j.easy-rag.path=tintin-docs` is needed.

**TintinSourceAugmenter** implements `AiResponseAugmenter<String>` and appends source citations to every RAG response, showing which document segments informed the answer.

```bash
curl -s -X POST http://localhost:8080/api/tintin/ask \
  -H 'Content-Type: application/json' \
  -d '{"question":"What happens in Tintin in the Land of the Soviets?"}'
```

### 4. Function Calling and Tools

**TintinTrivia** uses `@ToolBox` to give the LLM access to two `@Tool`-annotated CDI beans:

- **TintinFactsTool** -- looks up publication years for all 24 Tintin albums, lists all adventures in order, and retrieves character rosters per adventure.
- **DateCalculatorTool** -- calculates how many years ago a given year was, or the span between two years.

The LLM chains tool calls autonomously. For example, asking *"How many years ago was Tintin in the Land of the Soviets published?"* triggers a lookup of the publication year (1930) followed by a years-ago calculation.

```bash
curl -s -X POST http://localhost:8080/api/tintin/trivia \
  -H 'Content-Type: application/json' \
  -d '{"question":"How many years ago was Tintin in the Land of the Soviets published?"}'
```

### 5. Image and Vision Processing

**CoverAnalyzer** demonstrates two approaches to passing images to GPT-4o's vision capabilities:

- `describeCover(@ImageUrl String imageUrl)` -- passes a URL pointing to the locally served cover image.
- `analyzeArtStyle(Image image)` -- reads the image from the classpath, base64-encodes it, and sends it as a `dev.langchain4j.data.image.Image` object.

```bash
# Via image URL
curl -s -X POST http://localhost:8080/api/tintin/cover/describe

# Via base64-encoded image
curl -s -X POST http://localhost:8080/api/tintin/cover/analyze
```

### 6. Few-Shot Prompting

**AdventureClassifier** embeds three example input/output pairs directly in the `@SystemMessage`, demonstrating the few-shot prompting pattern. The examples teach the model how to classify adventure descriptions into categories (MYSTERY, POLITICAL_INTRIGUE, EXPLORATION, TREASURE_HUNT, SCIENCE_FICTION, RESCUE_MISSION) with reasoning and a confidence score.

```bash
curl -s -X POST http://localhost:8080/api/tintin/classify \
  -H 'Content-Type: application/json' \
  -d '{"description":"Tintin infiltrates Soviet Russia to report on the Bolshevik regime and uncovers propaganda."}' | jq
```

### 7. WebSocket Chat with Session Memory

**AdventureChatBot** is a `@SessionScoped` AI service that speaks as Tintin in first person. The WebSocket endpoint at `/adventure-chat` automatically binds chat memory to each WebSocket connection -- no `@MemoryId` annotation needed. Memory is configured with a 20-message sliding window.

Open [http://localhost:8080/chat.html](http://localhost:8080/chat.html) for the chat UI.

### 8. Streaming Responses (SSE)

The `chatStreaming()` method on `AdventureChatBot` returns `Multi<String>` for token-by-token streaming. The `StreamingChatResource` exposes this as Server-Sent Events:

```bash
curl -N http://localhost:8080/api/tintin/chat/stream?message=Tell+me+about+your+adventure+in+Tibet
```

### 9. Input and Output Guardrails

**GuardedTintinService** demonstrates both guardrail types:

- **TintinTopicGuardrail** (`InputGuardrail`) -- rejects off-topic queries containing keywords like "stock price", "cryptocurrency", "recipe", or "medical advice" with a Captain Haddock-style error message.
- **SafeContentGuardrail** (`OutputGuardrail`) -- validates that responses are non-empty and under 2000 characters. If validation fails, the LLM is asked to retry (up to 3 attempts).

```bash
# On-topic (passes guardrails)
curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' \
  -d '{"question":"Who is Tintin?"}'

# Off-topic (blocked by input guardrail)
curl -s -X POST http://localhost:8080/api/tintin/guarded \
  -H 'Content-Type: application/json' \
  -d '{"question":"What is the stock price of AAPL?"}'
```

### 10. Content Moderation

**ModeratedTintinService** uses the `@Moderate` annotation to invoke OpenAI's moderation API (`omni-moderation-latest`) on every response. The service is positioned as a family-friendly Tintin expert for young readers.

```bash
curl -s -X POST http://localhost:8080/api/tintin/moderated \
  -H 'Content-Type: application/json' \
  -d '{"question":"Tell me about Snowy the dog"}'
```

### 11. Observability

The application includes both metrics and tracing out of the box:

- **Metrics** (Micrometer + Prometheus) -- every AI service method is automatically instrumented with `langchain4j.aiservices.timed` and `langchain4j.aiservices.counted` metrics. View at [http://localhost:8080/q/metrics](http://localhost:8080/q/metrics).
- **Tracing** (OpenTelemetry) -- each AI service call creates a span (`langchain4j.aiservices.<service>.<method>`), and tool invocations create child spans (`langchain4j.tools.<tool>`).

### 12. Dev UI

Quarkus Dev UI is available at [http://localhost:8080/q/dev-ui](http://localhost:8080/q/dev-ui) in dev mode. It provides:

- A table of all registered AI Services and their tools
- An inventory of all `@Tool`-annotated methods
- An interactive chat interface for testing models directly
- Moderation model testing

## API Reference

| Method | Endpoint | Feature | Input |
|--------|----------|---------|-------|
| `POST` | `/api/tintin/ask` | RAG + EasyRAG + citations | `{"question": "..."}` |
| `POST` | `/api/tintin/character` | Structured output (records) | `{"name": "..."}` |
| `POST` | `/api/tintin/classify` | Few-shot prompting | `{"description": "..."}` |
| `POST` | `/api/tintin/cover/describe` | Image via `@ImageUrl` | (no body) |
| `POST` | `/api/tintin/cover/analyze` | Image via base64 `Image` | (no body) |
| `POST` | `/api/tintin/trivia` | Tools + @ToolBox + chaining | `{"question": "..."}` |
| `POST` | `/api/tintin/guarded` | Input + output guardrails | `{"question": "..."}` |
| `POST` | `/api/tintin/moderated` | Content moderation | `{"question": "..."}` |
| `GET` | `/api/tintin/chat/stream` | SSE streaming | `?message=...` |
| `WS` | `/adventure-chat` | WebSocket chat + memory | text frames |

## Tech Stack

| Component | Technology |
|-----------|------------|
| Framework | Quarkus 3.34.3 |
| AI Integration | Quarkus Langchain4j (via platform BOM) |
| LLM Provider | OpenAI GPT-4o |
| Moderation | OpenAI omni-moderation-latest |
| Embeddings | In-process ONNX (e5-small-v2, via EasyRAG) |
| Vector Store | In-memory (via EasyRAG) |
| PDF Parsing | Apache Tika (via EasyRAG) |
| REST | Quarkus REST + Jackson |
| WebSocket | quarkus-websockets-next |
| Metrics | Micrometer + Prometheus |
| Tracing | OpenTelemetry |
| Java | 17+ |

## Configuration

All configuration is in `src/main/resources/application.properties`. Key settings:

| Property | Value | Purpose |
|----------|-------|---------|
| `quarkus.langchain4j.openai.chat-model.model-name` | `gpt-4o` | Chat model |
| `quarkus.langchain4j.openai.moderation-model.model-name` | `omni-moderation-latest` | Moderation model |
| `quarkus.langchain4j.easy-rag.path` | `tintin-docs` | Directory scanned for RAG documents |
| `quarkus.langchain4j.easy-rag.max-segment-size` | `200` | Max tokens per document segment |
| `quarkus.langchain4j.easy-rag.max-results` | `5` | Number of segments returned per query |
| `quarkus.langchain4j.chat-memory.memory-window.max-messages` | `20` | Chat memory sliding window |
| `quarkus.langchain4j.guardrails.max-retries` | `3` | Output guardrail retry limit |
| `quarkus.langchain4j.response-schema` | `true` | Enable JSON schema injection for structured output |

## Quarkus Langchain4j Feature Coverage

| Feature | Where Demonstrated |
|---------|-------------------|
| `@RegisterAiService` | All 7 AI service interfaces |
| `@SystemMessage` / `@UserMessage` | All AI service methods |
| Structured output (Java records) | `CharacterAnalyzer`, `AdventureClassifier` |
| Qute template variables | `{name}`, `{description}`, `{question}` in `@UserMessage` |
| `@Tool` on CDI beans | `TintinFactsTool`, `DateCalculatorTool` |
| `@ToolBox` (method-level tools) | `TintinTrivia.answerTrivia()` |
| Tool chaining | TintinTrivia (year lookup followed by age calculation) |
| EasyRAG (zero-code RAG) | `TintinExpert` + PDF in `tintin-docs/` |
| `AiResponseAugmenter` | `TintinSourceAugmenter` (appends source citations) |
| `@ImageUrl` | `CoverAnalyzer.describeCover()` |
| `Image` type (base64) | `CoverAnalyzer.analyzeArtStyle()` |
| Few-shot prompting | `AdventureClassifier` (3 examples in `@SystemMessage`) |
| WebSocket chat | `AdventureChatWebSocket` at `/adventure-chat` |
| `@SessionScoped` + auto memory | `AdventureChatBot` |
| `Multi<String>` streaming | `StreamingChatResource` via SSE |
| `InputGuardrail` | `TintinTopicGuardrail` (topic filtering) |
| `OutputGuardrail` | `SafeContentGuardrail` (length + emptiness check) |
| `@Moderate` | `ModeratedTintinService` |
| Micrometer metrics | Automatic via `quarkus-micrometer-registry-prometheus` |
| OpenTelemetry tracing | Automatic via `quarkus-opentelemetry` |
| Dev UI | Automatic at `/q/dev-ui` in dev mode |
