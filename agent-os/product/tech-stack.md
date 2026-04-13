# Tech Stack

## Backend

- **Quarkus** (latest stable, ~3.x) — supersonic subatomic Java framework
- **Quarkus Langchain4j** extension — AI/LLM integration
- **Java 17+** — minimum language version
- **Maven** — build tool

## LLM Provider

- **OpenAI GPT-4o** — primary chat model (supports vision, function calling, moderation)
- **OpenAI Moderation API** — content moderation model
- **In-process ONNX embedding** (e5-small-v2) — via EasyRAG for vector embeddings

## Frontend

- **Static HTML/CSS/JS** — served from `META-INF/resources/`
- **wc-chatbot** web component — pre-built chat UI
- **Lit** — web component framework (for the chatbot)
- **ES module import maps** — via mvnpm, no build tooling needed

## Database

- **In-memory vector store** — via EasyRAG (no external DB needed for demo)

## Other

- **WebSockets (quarkus-websockets-next)** — real-time chat
- **Micrometer + Prometheus** — metrics
- **OpenTelemetry** — distributed tracing
- **Apache Tika** — PDF text extraction (via EasyRAG)
