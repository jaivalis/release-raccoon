package com.raccoon.notify;

import com.raccoon.entity.User;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/notify-users")
public class NotifyingResource {

    @Inject
    NotifyService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<User> scrapeReleases() {
        return service.notifyUsers();
    }
}
