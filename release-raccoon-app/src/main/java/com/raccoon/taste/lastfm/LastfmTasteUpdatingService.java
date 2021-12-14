package com.raccoon.taste.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.lastfm.LastfmScraper;
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

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.taste.Util.normalizeWeights;

@Slf4j
@ApplicationScoped
public class LastfmTasteUpdatingService implements TasteUpdatingService {

    UserArtistFactory userArtistFactory;
    UserRepository userRepository;
    LastfmScraper lastfmScraper;
    NotifyService notifyService;

    @Inject
    public LastfmTasteUpdatingService(final UserArtistFactory userArtistFactory,
                                      final UserRepository userRepository,
                                      final LastfmScraper lastfmScraper,
                                      final NotifyService notifyService) {
        this.userArtistFactory = userArtistFactory;
        this.userRepository = userRepository;
        this.lastfmScraper = lastfmScraper;
        this.notifyService = notifyService;
    }

    public User updateTaste(final Long userId) {
        var user = userRepository.findById(userId);

        if (!canUpdateTaste(user)) {
            return user;
        }

        final Collection<MutablePair<Artist, Float>> aggregateTaste = new ArrayList<>(lastfmScraper.scrapeTaste(user.getLastfmUsername(), Optional.of(50)));

        // Keep track of the artists that might be relevant to get updates from
        // That is the ones that were already in the database.
        final List<UserArtist> existingArtists = new ArrayList<>();
        LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);

        user.setArtists(
                normalizeWeights(aggregateTaste)
                        .stream()
                        .map(pair -> {
                            var artist = pair.left;
                            final var userArtist = userArtistFactory.getOrCreateUserArtist(user, artist);

                            userArtist.setWeight(pair.right);

                            if (artist.getCreateDate() == null || twoMinutesAgo.isAfter(artist.getCreateDate())) {
                                // artist existed in the database prior, might have a release
                                existingArtists.add(userArtist);
                            }

                            return userArtist;
                        }).collect(Collectors.toSet())
        );
        user.setLastLastFmScrape(LocalDateTime.now());

        userRepository.persist(user);

        notifyForRecentReleases(user, existingArtists);

        return user;
    }

    @Override
    public void notifyForRecentReleases(User user, Collection<UserArtist> userArtists) {
        notifyService.notifySingleUser(user, userArtists);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean canUpdateTaste(User user) {
        if (StringUtil.isNullOrEmpty(user.getLastfmUsername())) {
            log.warn("User lastfm username not set, skipping.");
            return false;
        }
        if (!user.isLastfmScrapeRequired(1)) {
            log.info("User was lastfm scraped not long ago, skipping.");
            return false;
        }
        return true;
    }
}
