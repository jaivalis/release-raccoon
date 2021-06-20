package com.raccoon.taste.spotify;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.User;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.net.URI;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/spotify-auth-callback")
public class SpotifyAuthResource {

    @Inject
    SpotifyUserAuthorizer spotifyAuthService;
    @Inject
    SpotifyTasteUpdatingService spotifyTasteUpdatingService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response authorizeSpotify(@Valid RegisterUserRequest request) {
        spotifyAuthService.authorizationCodeUriAsync(request.getEmail());
        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeAuth(@QueryParam("code") final String code,
                                 @QueryParam("state") final String state,
                                 @QueryParam("error") final String error) {
        log.info("Received GET {} {} {}", code, state, error);
        if (error != null) {
            log.error("An error occurred with Spotify authentication");
            return Response.noContent().build();
        }

        // Request access and refresh tokens
        spotifyAuthService.requestAuthorization(code);
        var redirect = URI.create("http://localhost:8080/spotify-auth-callback/user-top-artists?userId=" + state);
        return Response.temporaryRedirect(redirect).build();
    }

    @GET
    @Path("/user-top-artists")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public User getUserTopArtists(@QueryParam("userId") final String userId) {
        Optional<User> existing = User.findByIdOptional(Long.valueOf(userId));
        if (existing.isEmpty()) {
            throw new NotFoundException("User not found");
        }

        return spotifyTasteUpdatingService.updateTaste(existing.get());
    }

}