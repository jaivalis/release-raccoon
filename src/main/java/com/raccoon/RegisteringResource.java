package com.raccoon;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.scraper.taste.UserRegisteringService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/register")
public class RegisteringResource {

    @Inject
    UserRegisteringService service;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String register(RegisterUserRequest request) {
        service.register(request);
        return "User Registered (?)";
    }

}