package com.raccoon.registration;

import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserFactory;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.util.Optional;

import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.entity.User.findByEmailOptional;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/register")
@Slf4j
@Authenticated
public class UserRegisteringResource {

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

        Optional<User> existing = findByEmailOptional(email);
        if (existing.isPresent()) {
            log.info("User with email {} exists.", email);
            return Response.status(CONFLICT).build();
        }
        var user = UserFactory.getOrCreateUser(email);
        user.setLastfmUsername(lastfmUsername);
        user.setSpotifyEnabled(spotifyEnabled);
        user.setUsername(username);
        user.persist();

        return Response.ok(user).build();
    }

    @Transactional
    @GET
    @Path("/enableServices")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response setLastfmUsername(@QueryParam("lastfmUsername") final Optional<String> lastfmUsernameOpt,
                                      @QueryParam("enableSpotify") final Optional<Boolean> enableSpotifyOpt) {
        final String email = idToken.getClaim("email");

        Optional<User> existing = findByEmailOptional(email);
        if (existing.isEmpty()) {
            log.info("User does not exist.");
            return Response.status(NOT_FOUND).build();
        }
        var user = UserFactory.getOrCreateUser(email);
        lastfmUsernameOpt.ifPresent(user::setLastfmUsername);
        enableSpotifyOpt.ifPresent(user::setSpotifyEnabled);
        user.persist();

        return Response.ok(user).build();
    }

}