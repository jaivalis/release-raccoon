package com.raccoon.taste.spotify;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.scraper.config.SpotifyConfig;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.net.URI;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.GET;
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
    @Inject
    SpotifyConfig config;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response authorizeSpotify(@Valid RegisterUserRequest request) {
        spotifyAuthService.authorizationCodeUriAsync(request.email());
        return Response.noContent().build();
    }

    /**
     * Gets invoked by spotify auth as a callback to complete auth flow.
     *
     * @param code
     * @param state Provided by {@code com.raccoon.scraper.spotify.SpotifyUserAuthorizer#authorizationCodeUriSync}, currently set as the userId
     * @param error Should be null
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response completeAuth(@QueryParam("code") final String code,
                                 @QueryParam("state") final String state,
                                 @QueryParam("error") final String error) {
        log.info("Received GET code: {}, state: {}, error: {}", code, state, error);
        if (error != null) {
            log.error("An error occurred with Spotify authentication: {}", error);
            return Response.noContent().build();
        }

        // Request access and refresh tokens
        spotifyAuthService.requestAuthorization(code);
        var redirect = URI.create(config.authCallbackUri() + "user-top-artists?userId=" + state);
        return Response.temporaryRedirect(redirect).build();
    }

    @GET
    @Path("/user-top-artists")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getUserTopArtists(@QueryParam("userId") final String userId) {
        spotifyTasteUpdatingService.updateTaste(Long.valueOf(userId));
        return Response.temporaryRedirect(URI.create("/me")).build();
    }

}