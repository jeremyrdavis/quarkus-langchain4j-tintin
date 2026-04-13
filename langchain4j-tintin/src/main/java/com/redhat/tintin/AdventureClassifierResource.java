package com.redhat.tintin;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/tintin/classify")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdventureClassifierResource {

    @Inject
    AdventureClassifier classifier;

    public record ClassifyRequest(String description) {
    }

    @POST
    public ClassifiedAdventure classify(ClassifyRequest request) {
        return classifier.classify(request.description());
    }
}
