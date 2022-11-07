package com.raccoon.taste.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.lastfm.LastfmScraper;
import com.raccoon.taste.TasteScrapeArtistWeightPairProcessor;
import com.raccoon.taste.TasteUpdatingService;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.taste.Util.normalizeWeights;

@Slf4j
@ApplicationScoped
public class LastfmTasteUpdatingService implements TasteUpdatingService {

    final UserRepository userRepository;
    final LastfmScraper lastfmScraper;
    final NotifyService notifyService;
    final TasteScrapeArtistWeightPairProcessor tasteScrapeArtistWeightPairProcessor;

    @Inject
    public LastfmTasteUpdatingService(final TasteScrapeArtistWeightPairProcessor tasteScrapeArtistWeightPairProcessor,
                                      final UserRepository userRepository,
                                      final LastfmScraper lastfmScraper,
                                      final NotifyService notifyService) {
        this.tasteScrapeArtistWeightPairProcessor = tasteScrapeArtistWeightPairProcessor;
        this.userRepository = userRepository;
        this.lastfmScraper = lastfmScraper;
        this.notifyService = notifyService;
    }

    public RaccoonUser updateTaste(final Long userId) {
        var user = userRepository.findById(userId);

        if (!hasLastfmEnabledAndIsScrapeRequired(user)) {
            return user;
        }

        final Collection<MutablePair<Artist, Float>> aggregateTaste =
                new ArrayList<>(
                        lastfmScraper.scrapeTaste(user.getLastfmUsername(), Optional.of(50))
                );

        // Keep track of the artists that might be relevant to get updates from
        // Those are the ones that were already in the database.
        final Set<UserArtist> existingArtists = new HashSet<>();

        user.setArtists(
                normalizeWeights(aggregateTaste)
                        .stream()
                        .map(pair -> {
                            var artist = pair.left;
                            var weight = pair.right;

                            return tasteScrapeArtistWeightPairProcessor
                                    .delegateProcessArtistWeightPair(
                                            user, artist, weight, existingArtists
                                    );
                        }).collect(Collectors.toSet())
        );
        user.setLastLastFmScrape(LocalDateTime.now());

        userRepository.persist(user);

        notifyForRecentReleases(user, existingArtists);
        log.info("Existing artists: {}", existingArtists);

        return user;
    }

    @Override
    public void notifyForRecentReleases(RaccoonUser raccoonUser, Collection<UserArtist> userArtists) {
        notifyService.notifySingleUser(raccoonUser, userArtists)
                .await().indefinitely();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean hasLastfmEnabledAndIsScrapeRequired(RaccoonUser raccoonUser) {
        if (StringUtil.isNullOrEmpty(raccoonUser.getLastfmUsername())) {
            log.warn("RaccoonUser lastfm username not set, skipping.");
            return false;
        }
        if (!raccoonUser.isLastfmScrapeRequired(1)) {
            log.info("RaccoonUser was lastfm scraped not long ago, skipping.");
            return false;
        }
        return true;
    }
}
