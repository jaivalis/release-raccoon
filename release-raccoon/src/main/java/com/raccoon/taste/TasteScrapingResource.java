package com.raccoon.taste;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.taste.spotify.SpotifyTasteUpdatingService;

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

import lombok.extern.slf4j.Slf4j;

import static com.raccoon.entity.User.findByEmailOptional;

/**
 * Utility class to scrape taste. When deployed this service will be invoked into a cron-job.
 */
@Slf4j
@Path("/scrape-taste")
public class TasteScrapingResource {

    @Inject
    LastfmTasteUpdatingService lastfmService;
    @Inject
    SpotifyTasteUpdatingService spotifyTasteUpdatingService;

    @GET
    @Path("lastfm")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Collection<UserArtist> scrapeLastfmTaste(@QueryParam("email") final String email) {
        var existing = getUser(email);
        final var updated = lastfmService.updateTaste(existing);

        return updated.getArtists();
    }

    @GET
    @Path("spotify")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response scrapeSpotifyTaste(@QueryParam("userId") final Long userId) {
        return spotifyTasteUpdatingService.scrapeTaste(userId);
    }

    private User getUser(@QueryParam("email") String email) {
        Optional<User> existing = findByEmailOptional(email);
        if (existing.isEmpty()) {
            log.warn("User with email {} not found.", email);
            throw new NotFoundException("Unknown user with email: " + email);
        }
        return existing.get();
    }

}