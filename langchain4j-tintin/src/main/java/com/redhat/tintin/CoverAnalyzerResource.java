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
import java.util.Base64;

@Path("/api/tintin/cover")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class CoverAnalyzerResource {

    @Inject
    CoverAnalyzer analyzer;

    @POST
    @Path("/describe")
    public String describeCover() {
        String imageUrl = "http://localhost:8080/images/tintin-image.png";
        return analyzer.describeCover(imageUrl);
    }

    @POST
    @Path("/analyze")
    public String analyzeArtStyle() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/images/tintin-image.png")) {
            if (is == null) {
                return "Error: tintin-image.png not found in resources";
            }
            byte[] bytes = is.readAllBytes();
            String b64 = Base64.getEncoder().encodeToString(bytes);
            Image image = Image.builder()
                    .base64Data(b64)
                    .mimeType("image/png")
                    .build();
            return analyzer.analyzeArtStyle(image);
        }
    }
}
