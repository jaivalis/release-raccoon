package com.raccoon.taste;

import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.taste.lastfm.LastfmTasteUpdatingService;
import com.raccoon.taste.spotify.SpotifyTasteUpdatingService;
import com.raccoon.taste.spotify.dto.SpotifyAuth;

import org.jboss.resteasy.reactive.RestQuery;

import java.util.Collection;
import java.util.Optional;

import io.quarkus.oidc.UserInfo;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/scrape-taste")
public class TasteScrapingResource {

    final UserInfo userInfo;

    LastfmTasteUpdatingService lastfmTasteUpdatingService;
    SpotifyTasteUpdatingService spotifyTasteUpdatingService;

    UserRepository userRepository;

    @Inject
    public TasteScrapingResource(LastfmTasteUpdatingService lastfmTasteUpdatingService,
                                 SpotifyTasteUpdatingService spotifyTasteUpdatingService,
                                 UserRepository userRepository,
                                 UserInfo userInfo) {
        this.lastfmTasteUpdatingService = lastfmTasteUpdatingService;
        this.spotifyTasteUpdatingService = spotifyTasteUpdatingService;
        this.userRepository = userRepository;
        this.userInfo = userInfo;
    }

    @GET
    @Path("lastfm")
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    @Transactional
    public Collection<UserArtist> scrapeLastfmTaste() {
        final String email = userInfo.getEmail();
        var existing = getUser(email);
        final var updated = lastfmTasteUpdatingService.updateTaste(existing.id);
        return updated.getArtists();
    }

    /**
     * Used when the application runs in WebApp mode.
     */
    @GET
    @Path("spotify")
    @Produces(MediaType.TEXT_PLAIN)
    @Authenticated
    @Transactional
    public Response scrapeSpotifyTasteWebApp() {
        final String email = userInfo.getEmail();
        var existing = getUser(email);
        return spotifyTasteUpdatingService.scrapeTaste(existing.id);
    }
    
    /**
     * Used for client-side Spotify authentication.
     * The UI handles the OAuth flow and sends back the authorization code and state.
     * @param auth SpotifyAuth containing code and state from client-side OAuth
     * @return Collection of UserArtists after scraping
     */
    @POST
    @Path("spotify/client-auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    @Transactional
    public Collection<UserArtist> scrapeSpotifyTasteClientAuth(SpotifyAuth auth) {
        final String email = userInfo.getEmail();
        var existing = getUser(email);
        return spotifyTasteUpdatingService.scrapeTasteWithClientAuth(existing.id, auth.code(), auth.state());
    }

    private RaccoonUser getUser(@RestQuery("email") String email) {
        Optional<RaccoonUser> existing = userRepository.findByEmailOptional(email);
        if (existing.isEmpty()) {
            log.warn("RaccoonUser with email {} not found.", email);
            throw new NotFoundException("Unknown raccoonUser with email: " + email);
        }
        return existing.get();
    }

}