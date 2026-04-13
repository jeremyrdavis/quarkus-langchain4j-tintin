# Product Roadmap

## Phase 1: MVP — Core AI Services

- Project scaffolding (Maven, dependencies, configuration)
- CharacterAnalyzer: structured output with Java records
- TintinExpert: RAG with EasyRAG over the Tintin PDF + source citations
- TintinTrivia: function calling with @Tool, @ToolBox, and ToolProvider
- CoverAnalyzer: image/vision processing with the book cover
- AdventureClassifier: few-shot prompting

## Phase 2: Interactive Features

- AdventureChatBot: WebSocket-based real-time chat as Tintin
- Streaming responses via SSE (Multi<String>)
- Chat memory with @SessionScoped and memory window config

## Phase 3: Safety & Observability

- Input guardrails (topic filtering)
- Output guardrails (content validation)
- Content moderation with @Moderate
- Micrometer metrics + OpenTelemetry tracing
- Dev UI showcase

## Phase 4: Post-Launch

- MCP server integration demo
- Additional document types in RAG (CSV guide)
- Fault tolerance patterns
- Semantic compression of chat history
- Ollama as an alternative local provider
