package com.raccoon.registration;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.User;

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
    @Produces(MediaType.APPLICATION_JSON)
    public User register(RegisterUserRequest request) {
        return service.register(request);
    }

}