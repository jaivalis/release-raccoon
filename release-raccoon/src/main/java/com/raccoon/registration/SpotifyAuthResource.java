package com.raccoon.registration;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.Artist;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.scraper.spotify.SpotifyUserAuth;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.hc.core5.http.ParseException;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

@Path("/spotify-auth-callback")
@Slf4j
public class SpotifyAuthResource {

    @Inject
    SpotifyUserAuth spotifyAuthService;

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
        var accessToken = spotifyAuthService.requestAuthorization(code);
        var redirect = URI.create("http://localhost:8080/spotify-auth-callback/user-top-artists");
        return Response.temporaryRedirect(redirect).build();
    }

    @Transactional
    @GET
    @Path("/user-top-artists")
    @Produces(MediaType.APPLICATION_JSON)
    public List<MutablePair<Artist, Float>> getUserTopArtists() {
        return getTopArtists();
    }

    public List<MutablePair<Artist, Float>> getTopArtists() {
        List<MutablePair<Artist, Float>> artists = new ArrayList<>();
        int offset = 0;
        Paging<com.wrapper.spotify.model_objects.specification.Artist> response;
        try {
            do {
                response = spotifyAuthService.executeGetUsersTopArtists(offset);
                artists.addAll(
                        Arrays.stream(response.getItems())
                                .map(artistObj ->
                                        MutablePair.of(processArtist(artistObj), (float) 1) //(float) artistObj.getPlaycount())
                                ).collect(Collectors.toList())
                );
                offset = response.getOffset() + response.getLimit();
            } while(response.getNext() != null);
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            log.error("Something went wrong when fetching Spotify artists ", e);
        }
        return artists;
    }

    public Artist processArtist(Object entry) {
        log.info("Got entry: {}", entry);
        if (entry instanceof com.wrapper.spotify.model_objects.specification.Artist) {
            com.wrapper.spotify.model_objects.specification.Artist spotifyArtist
                    = (com.wrapper.spotify.model_objects.specification.Artist) entry;
            return ArtistFactory.getOrCreateArtist(spotifyArtist.getName());
        }
        return null;
    }

}