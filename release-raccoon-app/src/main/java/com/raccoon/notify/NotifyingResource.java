package com.raccoon.notify;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/notify-users")
public class NotifyingResource {

    NotifyService service;

    @Inject
    public NotifyingResource(NotifyService service) {
        this.service = service;
    }

    @GET
    @Blocking
    public Uni<Boolean> notifyUsers() {
        return service.notifyUsers();
    }

}
