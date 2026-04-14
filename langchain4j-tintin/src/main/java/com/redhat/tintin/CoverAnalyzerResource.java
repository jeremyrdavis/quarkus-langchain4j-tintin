package com.redhat.tintin;

import dev.langchain4j.data.image.Image;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Path("/api/tintin/cover")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class CoverAnalyzerResource {

    public record CoverRequest(String url) {}

    @Inject
    CoverAnalyzer analyzer;

    @POST
    @Path("/describe")
    public String describeCover(CoverRequest request) throws IOException {
        Image image = loadImage(request);
        return analyzer.describeCover(image);
    }

    @POST
    @Path("/analyze")
    public String analyzeArtStyle(CoverRequest request) throws IOException {
        Image image = loadImage(request);
        return analyzer.analyzeArtStyle(image);
    }

    private Image loadImage(CoverRequest request) throws IOException {
        if (request != null && request.url() != null && !request.url().isBlank()) {
            return downloadImage(request.url());
        }
        return loadBundledImage();
    }

    private Image downloadImage(String url) throws IOException {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to download image: HTTP " + response.statusCode());
            }

            byte[] bytes = response.body();
            String b64 = Base64.getEncoder().encodeToString(bytes);

            String mimeType = response.headers()
                    .firstValue("Content-Type")
                    .orElseGet(() -> guessMimeType(url));

            return Image.builder()
                    .base64Data(b64)
                    .mimeType(mimeType)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Image download interrupted", e);
        }
    }

    private String guessMimeType(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/png";
    }

    private Image loadBundledImage() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/images/tintin-image.png")) {
            if (is == null) {
                throw new IOException("tintin-image.png not found in resources");
            }
            byte[] bytes = is.readAllBytes();
            String b64 = Base64.getEncoder().encodeToString(bytes);
            return Image.builder()
                    .base64Data(b64)
                    .mimeType("image/png")
                    .build();
        }
    }
}
