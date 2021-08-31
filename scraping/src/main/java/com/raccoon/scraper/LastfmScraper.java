package com.raccoon.scraper;

import com.raccoon.entity.Artist;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.scraper.config.LastFmConfig;

import de.umass.lastfm.Period;
import de.umass.lastfm.User;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@ApplicationScoped
public class LastfmScraper implements TasteScraper {

    ArtistFactory artistFactory;

    final String apiKey;

    @Inject
    public LastfmScraper(final LastFmConfig config,
                         final ArtistFactory artistFactory) {
        this.apiKey = config.getApiKey();
        this.artistFactory = artistFactory;
    }

    @Override
    public Collection<MutablePair<Artist, Float>> scrapeTaste(final String username,
                                                              final Optional<Integer> limit) {
        final Set<de.umass.lastfm.Artist> topArtists = new HashSet<>();
        final Set<String> seenNames = new HashSet<>();
        mergeArtists(topArtists, User.getTopArtists(username, Period.OVERALL, apiKey), seenNames);
        mergeArtists(topArtists, User.getTopArtists(username, Period.TWELVE_MONTHS, apiKey), seenNames);
        mergeArtists(topArtists, User.getTopArtists(username, Period.SIX_MONTHS, apiKey), seenNames);
        mergeArtists(topArtists, User.getTopArtists(username, Period.THREE_MONTHS, apiKey), seenNames);
        mergeArtists(topArtists, User.getTopArtists(username, Period.ONE_MONTH, apiKey), seenNames);
        mergeArtists(topArtists, User.getTopArtists(username, Period.WEEK, apiKey), seenNames);

        return topArtists.stream()
                .map(artistObj -> MutablePair.of(processArtist(artistObj), (float) artistObj.getPlaycount()))
                .collect(Collectors.toList());
    }

    @Override
    public com.raccoon.entity.Artist processArtist(Object artistObj) {
        log.debug("{}", artistObj);
        if (artistObj instanceof de.umass.lastfm.Artist) {
            var lastfmArtist = (de.umass.lastfm.Artist) artistObj;

            return artistFactory.getOrCreateArtist(lastfmArtist.getName());
        }
        throw new IllegalArgumentException("Got an object type that is not supported.");
    }

    /**
     * Guarantees uniqueness of artists per name.
     * @param base collection to add to
     * @param from collection to add from
     * @param seenNames already processed names
     */
    private void mergeArtists(final Set<de.umass.lastfm.Artist> base,
                              final Collection<de.umass.lastfm.Artist> from,
                              final Set<String> seenNames) {
        for (val artist : from) {
            if (seenNames.contains(artist.getName())) {
                continue;
            }
            base.add(artist);
            seenNames.add(artist.getName());
        }
    }
}
