package com.redhat.tintin.guardrails;

import com.redhat.tintin.QueryRequest;
import dev.langchain4j.guardrail.GuardrailException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/tintin/guarded")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuardedResource {

    @Inject
    GuardedTintinService service;

    @POST
    public Response ask(QueryRequest request) {
        try {
            String answer = service.ask(request.question());
            return Response.ok(answer).build();
        } catch (GuardrailException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
