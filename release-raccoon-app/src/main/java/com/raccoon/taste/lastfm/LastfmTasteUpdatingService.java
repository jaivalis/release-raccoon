package com.raccoon.taste.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.scraper.LastfmScraper;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.taste.Util.normalizeWeights;

@Slf4j
@ApplicationScoped
public class LastfmTasteUpdatingService {

    @Inject
    UserArtistFactory userArtistFactory;
    @Inject
    UserRepository userRepository;
    @Inject
    LastfmScraper lastfmScraper;

    public LastfmTasteUpdatingService(final UserArtistFactory userArtistFactory,
                                      final UserRepository userRepository,
                                      final LastfmScraper lastfmScraper) {
        this.userArtistFactory = userArtistFactory;
        this.userRepository = userRepository;
        this.lastfmScraper = lastfmScraper;
    }

    public User updateTaste(final User user) {
        if (StringUtil.isNullOrEmpty(user.getLastfmUsername())) {
            log.warn("User lastfm username not set, skipping.");
            return user;
        }

        if (!userRepository.isLastfmScrapeRequired(1, user.getLastLastFmScrape())) {
            log.info("User was lastfm scraped not long ago, skipping.");
            return user;
        }

        final Collection<MutablePair<Artist, Float>> aggregateTaste = new ArrayList<>(lastfmScraper.scrapeTaste(user.getLastfmUsername(), Optional.of(50)));

        user.setArtists(
                normalizeWeights(aggregateTaste)
                        .stream()
                        .map(pair -> {
                            final var userArtist = userArtistFactory.getOrCreateUserArtist(user, pair.left);
                            userArtist.setWeight(pair.right);
                            return userArtist;
                        }).collect(Collectors.toSet())
        );
        user.setLastLastFmScrape(LocalDateTime.now());

        userRepository.persist(user);
        return user;
    }

}
