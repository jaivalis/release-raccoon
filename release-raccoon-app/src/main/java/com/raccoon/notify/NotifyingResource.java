package com.raccoon.notify;

import javax.inject.Inject;
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
    public Uni<Boolean> notifyUsers() {
        return service.notifyUsers();
    }

}
