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

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
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

    @Inject
    LastfmTasteUpdatingService lastfmTasteUpdatingService;
    @Inject
    SpotifyTasteUpdatingService spotifyTasteUpdatingService;

    @Inject
    UserRepository userRepository;

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