package com.raccoon.registration;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.EMAIL_CLAIM;
import static com.raccoon.Constants.LASTFM_USERNAME_CLAIM;
import static com.raccoon.Constants.SPOTIFY_ENABLED_CLAIM;
import static com.raccoon.Constants.USERNAME_CLAIM;

@Path("/register")
@Slf4j
@Authenticated
public class UserRegisteringResource {

    @Inject
    RegisteringService service;

    @IdToken
    JsonWebToken idToken;

    @Transactional
    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCallback() {
        final String username = idToken.getClaim(USERNAME_CLAIM);
        final String email = idToken.getClaim(EMAIL_CLAIM);
        final String lastfmUsername = idToken.getClaim(LASTFM_USERNAME_CLAIM);
        final Boolean spotifyEnabled = Boolean.parseBoolean(idToken.getClaim(SPOTIFY_ENABLED_CLAIM));

        var user = service.registerUser(username, email, lastfmUsername, spotifyEnabled);

        return Response.ok(user).build();
    }

}