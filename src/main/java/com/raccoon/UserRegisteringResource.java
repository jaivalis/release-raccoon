package com.raccoon;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.User;
import com.raccoon.scraper.taste.UserRegisteringService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/register")
public class UserRegisteringResource {

    @Inject
    UserRegisteringService service;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public User register(RegisterUserRequest request) {
        service.register(request);
        return null;
    }

}