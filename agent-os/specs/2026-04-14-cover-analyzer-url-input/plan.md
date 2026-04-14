# Cover Analyzer -- Accept Image URLs from Web UI

## Context

The Cover Analyzer currently only works with the bundled tintin-image.png. Both endpoints take no input and always analyze the same image. Adding URL support makes the demo interactive -- paste any image URL during a presentation and get a live analysis.

## Tasks

1. Save spec documentation
2. Update CoverAnalyzerResource to accept optional URL, download server-side, base64-encode
3. Update index.html dropdown -- merge options, add url field type
4. Update index.html JS -- send url in request body

## Files Modified

- `langchain4j-tintin/src/main/java/com/redhat/tintin/CoverAnalyzerResource.java`
- `langchain4j-tintin/src/main/resources/META-INF/resources/index.html`
