package com.raccoon.taste.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.scraper.LastfmScraper;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static com.raccoon.entity.User.persist;
import static com.raccoon.entity.factory.UserArtistFactory.getOrCreateUserArtist;

@Slf4j
@ApplicationScoped
public class LastfmTasteUpdatingService {

    @Inject
    LastfmScraper lastfmScraper;

    public User updateTaste(final User user) {
        if (StringUtil.isNullOrEmpty(user.getLastfmUsername())) {
            log.warn("User lastfm username not set, skipping.");
            return user;
        }

        if (!user.isLastfmScrapeRequired(1)) {
            log.info("User was lastfm scraped not long ago, skipping.");
            return user;
        }

        final Collection<MutablePair<Artist, Float>> aggregateTaste = new ArrayList<>(lastfmScraper.scrapeTaste(user.getLastfmUsername(), Optional.of(50)));

        user.setArtists(
                normalizeWeights(aggregateTaste)
                        .stream()
                        .map(pair -> {
                            final var userArtist = getOrCreateUserArtist(user, pair.left);
                            userArtist.setWeight(pair.right);
                            return userArtist;
                        }).collect(Collectors.toSet())
        );
        user.setLastLastFmScrape(LocalDateTime.now());

        persist(user);
        return user;
    }

    private Collection<MutablePair<Artist, Float>> normalizeWeights(Collection<MutablePair<Artist, Float>> taste) {
        float max = 0;
        for (val pair : taste) {
            max = Math.max(max, pair.getRight());
        }
        if (max == 0) {
            return Collections.emptyList();
        }
        final float maxWeight = max;
        taste.forEach(pair -> pair.setRight(pair.right / maxWeight));
        return taste;
    }
}
