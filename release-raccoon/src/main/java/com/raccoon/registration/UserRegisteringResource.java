package com.raccoon.registration;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserFactory;

import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import static com.raccoon.entity.User.findByEmailOptional;
import static javax.ws.rs.core.Response.Status.CONFLICT;

@Path("/register")
@Slf4j
public class UserRegisteringResource {

    @Transactional
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(@Valid RegisterUserRequest request) {
        Optional<User> existing = findByEmailOptional(request.getEmail());
        if (existing.isPresent()) {
            log.info("User with email {} exists.", request.getEmail());
            return Response.status(CONFLICT).build();
        }
        var user = UserFactory.getOrCreateUser(request.getEmail());
        user.setLastfmUsername(request.getLastfmUsername());
        user.setSpotifyEnabled(request.getSpotifyEnabled());

        return Response.ok(user).build();
    }

}