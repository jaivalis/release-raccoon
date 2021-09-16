package com.raccoon.taste.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import static com.raccoon.taste.Util.normalizeWeights;

@Slf4j
@ApplicationScoped
public class SpotifyTasteUpdatingService {

    @Inject
    UserArtistFactory userArtistFactory;
    @Inject
    UserRepository userRepository;

    @Inject
    SpotifyUserAuthorizer spotifyUserAuthorizer;
    @Inject
    SpotifyScraper spotifyScraper;

    public SpotifyTasteUpdatingService(final UserArtistFactory userArtistFactory,
                                       final UserRepository userRepository,
                                       final SpotifyUserAuthorizer spotifyUserAuthorizer,
                                       final SpotifyScraper spotifyScraper) {
        this.userArtistFactory = userArtistFactory;
        this.userRepository = userRepository;
        this.spotifyUserAuthorizer = spotifyUserAuthorizer;
        this.spotifyScraper = spotifyScraper;
    }

    /**
     * Responds with a redirect to spotify auth service
     *
     * @param userId userId as extracted from the Request
     * @return HTTP Redirect or NoContent
     */
    public Response scrapeTaste(Long userId) {
        final var byIdOptional = userRepository.findByIdOptional(userId);
        if (byIdOptional.isEmpty()) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }

        final var user = byIdOptional.get();

        if (Boolean.FALSE.equals(user.getSpotifyEnabled())) {
            log.warn("User with id {} has spotify disabled, skipping.", userId);
            return Response.noContent().build();
        }

        if (!user.isSpotifyScrapeRequired(1)) {
            log.info("User with id {} spotify taste was scraped not long ago, skipping.", userId);
            return Response.noContent().build();
        }

        log.info("Redirecting user with id {} to spotify auth service", userId);
        return Response
                .temporaryRedirect(spotifyUserAuthorizer.authorizationCodeUriSync(String.valueOf(userId)))
                .build();
    }

    public User updateTaste(final User user) {
        final Collection<MutablePair<Artist, Float>> spotifyTaste = spotifyScraper.fetchTopArtists(spotifyUserAuthorizer);

        user.setArtists(
                normalizeWeights(spotifyTaste)
                        .stream()
                        .map(pair -> {
                            final var userArtist = userArtistFactory.getOrCreateUserArtist(user, pair.left);
                            userArtist.setWeight(pair.right);
                            return userArtist;
                        }).collect(Collectors.toSet())
        );
        user.setLastSpotifyScrape(LocalDateTime.now());

        userRepository.persist(user);
        return user;
    }

}
