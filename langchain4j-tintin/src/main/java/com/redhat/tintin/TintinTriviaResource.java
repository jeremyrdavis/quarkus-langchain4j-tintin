package com.redhat.tintin;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/tintin/trivia")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TintinTriviaResource {

    @Inject
    TintinTrivia trivia;

    @POST
    public String askTrivia(QueryRequest request) {
        return trivia.answerTrivia(request.question());
    }
}
