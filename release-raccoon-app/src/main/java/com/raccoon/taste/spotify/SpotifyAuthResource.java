package com.raccoon.taste.spotify;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.scraper.config.SpotifyConfig;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.net.URI;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/spotify-auth-callback")
public class SpotifyAuthResource {

    SpotifyUserAuthorizer spotifyAuthService;
    SpotifyTasteUpdatingService spotifyTasteUpdatingService;
    SpotifyConfig config;

    public SpotifyAuthResource(SpotifyConfig config, SpotifyTasteUpdatingService spotifyTasteUpdatingService, SpotifyUserAuthorizer spotifyAuthService) {
        this.config = config;
        this.spotifyTasteUpdatingService = spotifyTasteUpdatingService;
        this.spotifyAuthService = spotifyAuthService;
    }

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