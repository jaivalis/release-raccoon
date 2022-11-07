package com.raccoon.taste.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;
import com.raccoon.taste.TasteScrapeArtistWeightPairProcessor;
import com.raccoon.taste.TasteUpdatingService;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import lombok.extern.slf4j.Slf4j;

import static com.raccoon.taste.Util.normalizeWeights;

@Slf4j
@ApplicationScoped
public class SpotifyTasteUpdatingService implements TasteUpdatingService {

    final TasteScrapeArtistWeightPairProcessor tasteScrapeArtistWeightPairProcessor;
    final UserRepository userRepository;
    final SpotifyUserAuthorizer spotifyUserAuthorizer;
    final SpotifyScraper spotifyScraper;
    final NotifyService notifyService;

    @Inject
    public SpotifyTasteUpdatingService(final TasteScrapeArtistWeightPairProcessor tasteScrapeArtistWeightPairProcessor,
                                       final UserRepository userRepository,
                                       final SpotifyUserAuthorizer spotifyUserAuthorizer,
                                       final SpotifyScraper spotifyScraper,
                                       final NotifyService notifyService) {
        this.tasteScrapeArtistWeightPairProcessor = tasteScrapeArtistWeightPairProcessor;
        this.userRepository = userRepository;
        this.spotifyUserAuthorizer = spotifyUserAuthorizer;
        this.spotifyScraper = spotifyScraper;
        this.notifyService = notifyService;
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
            throw new NotFoundException(String.format("RaccoonUser with id %s not found", userId));
        }

        final var user = byIdOptional.get();

        user.setSpotifyEnabled(true);
        if (!user.isSpotifyScrapeRequired(1)) {
            log.info("RaccoonUser with id {} spotify taste was scraped not long ago, skipping.", userId);
            return Response.noContent().build();
        }

        log.info("Redirecting raccoonUser with id {} to spotify auth service", userId);
        return Response
                .temporaryRedirect(spotifyUserAuthorizer.authorizationCodeUriSync(String.valueOf(userId)))
                .build();
    }

    public RaccoonUser updateTaste(final Long userId) {
        Optional<RaccoonUser> existing = userRepository.findByIdOptional(userId);
        if (existing.isEmpty()) {
            throw new NotFoundException("RaccoonUser not found");
        }
        var user = existing.get();

        final Collection<MutablePair<Artist, Float>> spotifyTaste = spotifyScraper.fetchTopArtists(spotifyUserAuthorizer);

        // Keep track of the artists that might be relevant to get updates from
        // That is the ones that were already in the database.
        final List<UserArtist> existingArtists = new ArrayList<>();

        user.setArtists(
                normalizeWeights(spotifyTaste)
                        .stream()
                        .map(
                                pair -> {
                                    var artist = pair.left;
                                    var weight = pair.right;

                                    return tasteScrapeArtistWeightPairProcessor
                                            .delegateProcessArtistWeightPair(
                                                    user, artist, weight, existingArtists
                                            );
                                }
                        ).collect(Collectors.toSet())
        );
        user.setSpotifyEnabled(true);
        user.setLastSpotifyScrape(LocalDateTime.now());

        userRepository.persist(user);

        notifyForRecentReleases(user, existingArtists);

        return user;
    }

    @Override
    public void notifyForRecentReleases(RaccoonUser raccoonUser, Collection<UserArtist> userArtists) {
        notifyService.notifySingleUser(raccoonUser, userArtists)
                .await().indefinitely();
    }

}
