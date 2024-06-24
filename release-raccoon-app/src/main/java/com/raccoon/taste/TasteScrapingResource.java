package com.raccoon.taste;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.taste.spotify.SpotifyTasteUpdatingService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import java.util.Collection;
import java.util.Optional;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.EMAIL_CLAIM;

/**
 * Utility class to scrape taste. When deployed this service will be invoked into a cron-job.
 */
@Slf4j
@Path("/scrape-taste")
public class TasteScrapingResource {

    @IdToken
    JsonWebToken idToken;

    LastfmTasteUpdatingService lastfmTasteUpdatingService;
    SpotifyTasteUpdatingService spotifyTasteUpdatingService;

    UserRepository userRepository;

    @Inject
    public TasteScrapingResource(LastfmTasteUpdatingService lastfmTasteUpdatingService, SpotifyTasteUpdatingService spotifyTasteUpdatingService, UserRepository userRepository) {
        this.lastfmTasteUpdatingService = lastfmTasteUpdatingService;
        this.spotifyTasteUpdatingService = spotifyTasteUpdatingService;
        this.userRepository = userRepository;
    }

    @GET
    @Path("lastfm")
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    @Transactional
    public Collection<UserArtist> scrapeLastfmTaste() {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        var existing = getUser(email);
        final var updated = lastfmTasteUpdatingService.updateTaste(existing.id);
        return updated.getArtists();
    }

    @GET
    @Path("spotify")
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    @Transactional
    public Response scrapeSpotifyTaste() {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        var existing = getUser(email);
        return spotifyTasteUpdatingService.scrapeTaste(existing.id);
    }

    private RaccoonUser getUser(@QueryParam("email") String email) {
        Optional<RaccoonUser> existing = userRepository.findByEmailOptional(email);
        if (existing.isEmpty()) {
            log.warn("RaccoonUser with email {} not found.", email);
            throw new NotFoundException("Unknown raccoonUser with email: " + email);
        }
        return existing.get();
    }

}