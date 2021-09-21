package com.raccoon.registration;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.util.Optional;

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
        final String username = idToken.getClaim("preferred_username");
        final String email = idToken.getClaim("email");
        final String lastfmUsername = idToken.getClaim("lastfm_username");
        final Boolean spotifyEnabled = Boolean.parseBoolean(idToken.getClaim("spotify_enabled"));

        var user = service.registerUser(username, email, lastfmUsername, spotifyEnabled);

        return Response.ok(user).build();
    }

    @Transactional
    @GET
    @Path("/enableServices")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response enableTasteSources(@QueryParam("lastfmUsername") final Optional<String> lastfmUsernameOpt,
                                       @QueryParam("enableSpotify") final Optional<Boolean> enableSpotifyOpt) {
        final String email = idToken.getClaim("email");

        var user = service.enableTasteSources(email, lastfmUsernameOpt, enableSpotifyOpt);

        return Response.ok(user).build();
    }

}