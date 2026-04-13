package com.redhat.tintin;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/tintin/ask")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class TintinExpertResource {

    @Inject
    TintinExpert expert;

    @POST
    public String ask(QueryRequest request) {
        return expert.askAboutTintin(request.question());
    }
}
