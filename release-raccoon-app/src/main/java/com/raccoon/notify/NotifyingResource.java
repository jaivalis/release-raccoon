package com.raccoon.notify;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.smallrye.mutiny.Uni;

@Path("/notify-users")
public class NotifyingResource {

    NotifyService service;

    @Inject
    public NotifyingResource(NotifyService service) {
        this.service = service;
    }

    @GET
//    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Uni<Boolean> notifyUsers() {
        return service.notifyUsers();
    }

}
