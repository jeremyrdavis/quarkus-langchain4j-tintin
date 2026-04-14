# Cover Analyzer URL Input -- Shaping Notes

## Scope

Allow the Cover Analyzer endpoints to accept an image URL from the web UI. The app downloads the image server-side, base64-encodes it, and sends it to OpenAI as an inline Image object. When no URL is provided, falls back to the bundled tintin-image.png.

## Decisions

- **Server-side download**: OpenAI cannot reach localhost URLs, so the Quarkus app fetches the image and sends it as base64. This works with any URL the server can reach.
- **Merge dropdown options**: The two separate "Cover Analyzer (URL)" and "Cover Analyzer (Base64)" dropdown entries become "Cover Analyzer (Describe)" and "Cover Analyzer (Art Style)" -- both now accept an optional URL.
- **Graceful fallback**: Blank URL input falls back to the bundled Tintin cover image, preserving current behavior.
- **MIME type detection**: Infer from the HTTP response Content-Type header, with fallback based on URL extension.

## Context

- **Visuals:** None -- the existing Try It Out panel is the reference
- **References:** CoverAnalyzerResource.java (current base64 loading pattern), index.html (dropdown + JS fetch logic)
- **Product alignment:** Improves demo interactivity for conference presentations (per mission.md)

## Standards Applied

- No formal standards directory exists yet
