package com.redhat.tintin.moderation;

import com.redhat.tintin.QueryRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/tintin/moderated")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class ModerationResource {

    @Inject
    ModeratedTintinService service;

    @POST
    public String askForKids(QueryRequest request) {
        return service.askForKids(request.question());
    }
}
