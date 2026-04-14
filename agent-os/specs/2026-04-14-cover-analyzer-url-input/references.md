# References for Cover Analyzer URL Input

## Similar Implementations

### CoverAnalyzerResource -- bundled image loading

- **Location:** `langchain4j-tintin/src/main/java/com/redhat/tintin/CoverAnalyzerResource.java`
- **Relevance:** Contains the existing `loadCoverImage()` method that reads from classpath and base64-encodes. The URL download follows the same pattern but sources bytes from HTTP instead of classpath.
- **Key patterns:** `Image.builder().base64Data(b64).mimeType("image/png").build()`

### index.html -- Try It Out panel

- **Location:** `langchain4j-tintin/src/main/resources/META-INF/resources/index.html`
- **Relevance:** The dropdown, textarea, and `sendRequest()` JS function. The `data-field` attribute on `<option>` elements controls what JSON field name is sent. Adding a `"url"` field type follows the same pattern as `"question"`, `"name"`, `"description"`.
- **Key patterns:** `data-field` attribute, `sendRequest()` body construction
