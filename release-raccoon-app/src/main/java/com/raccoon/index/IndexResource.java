package com.raccoon.index;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class IndexResource {

    IndexService service;

    @Inject
    public IndexResource(IndexService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        return Response.ok(service.getTemplateInstance()).build();
    }

}
