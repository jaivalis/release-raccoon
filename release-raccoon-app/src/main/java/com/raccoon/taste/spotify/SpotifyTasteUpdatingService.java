package com.raccoon.taste.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;
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

    UserArtistFactory userArtistFactory;
    UserRepository userRepository;
    SpotifyUserAuthorizer spotifyUserAuthorizer;
    SpotifyScraper spotifyScraper;
    NotifyService notifyService;

    @Inject
    public SpotifyTasteUpdatingService(final UserArtistFactory userArtistFactory,
                                       final UserRepository userRepository,
                                       final SpotifyUserAuthorizer spotifyUserAuthorizer,
                                       final SpotifyScraper spotifyScraper,
                                       final NotifyService notifyService) {
        this.userArtistFactory = userArtistFactory;
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
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }

        final var user = byIdOptional.get();

        user.setSpotifyEnabled(true);
        if (!user.isSpotifyScrapeRequired(1)) {
            log.info("User with id {} spotify taste was scraped not long ago, skipping.", userId);
            return Response.noContent().build();
        }

        log.info("Redirecting user with id {} to spotify auth service", userId);
        return Response
                .temporaryRedirect(spotifyUserAuthorizer.authorizationCodeUriSync(String.valueOf(userId)))
                .build();
    }

    public User updateTaste(final Long userId) {
        Optional<User> existing = userRepository.findByIdOptional(userId);
        if (existing.isEmpty()) {
            throw new NotFoundException("User not found");
        }
        var user = existing.get();

        final Collection<MutablePair<Artist, Float>> spotifyTaste = spotifyScraper.fetchTopArtists(spotifyUserAuthorizer);

        // Keep track of the artists that might be relevant to get updates from
        // That is the ones that were already in the database.
        final List<UserArtist> existingArtists = new ArrayList<>();
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);

        user.setArtists(
                normalizeWeights(spotifyTaste)
                        .stream()
                        .map(
                                artistWeightPair -> {
                                    var artist = artistWeightPair.left;
                                    var weight = artistWeightPair.right;

                                    var userArtist = userArtistFactory.getOrCreateUserArtist(user, artist);
                                    userArtist.setWeight(weight);

                                    if (artist.getCreateDate() == null || twoMinutesAgo.isAfter(artist.getCreateDate())) {
                                        // artist existed in the database prior, might have a release
                                        existingArtists.add(userArtist);
                                    }

                                    return userArtist;
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
    public void notifyForRecentReleases(User user, Collection<UserArtist> userArtists) {
        notifyService.notifySingleUser(user, userArtists);
    }

}
