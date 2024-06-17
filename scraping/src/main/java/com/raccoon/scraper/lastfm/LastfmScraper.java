package com.raccoon.scraper.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.scraper.TasteScraper;

import de.umass.lastfm.Period;

import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@ApplicationScoped
public class LastfmScraper implements TasteScraper {

    ArtistFactory artistFactory;
    ArtistRepository artistRepository;

    final RaccoonLastfmApi lastfmApi;

    @Inject
    public LastfmScraper(final ArtistFactory artistFactory,
                         final ArtistRepository artistRepository,
                         final RaccoonLastfmApi lastfmApi) {
        this.artistFactory = artistFactory;
        this.artistRepository = artistRepository;
        this.lastfmApi = lastfmApi;
    }

    @Override
    public Collection<MutablePair<Artist, Float>> scrapeTaste(final String username,
                                                              final Optional<Integer> limit) {
        final Set<de.umass.lastfm.Artist> topArtists = new HashSet<>();
        final Set<String> seenNames = new HashSet<>();

        for (Period period : Period.values()) {
            Collection<de.umass.lastfm.Artist> userTopArtists = lastfmApi.getUserTopArtists(username, period);
            mergeArtists(topArtists, userTopArtists, seenNames);
        }

        return topArtists.stream()
                .map(artistObj ->
                        MutablePair.of(processArtist(artistObj), (float) artistObj.getPlaycount()))
                .toList();
    }

    @Override
    public com.raccoon.entity.Artist processArtist(Object artistObj) {
        if (artistObj instanceof de.umass.lastfm.Artist lastfmArtist) {
            log.debug("Processing lastfm artist: {}", lastfmArtist.getName());
            var artist = artistFactory.getOrCreateArtist(lastfmArtist.getName());
            artist.setLastfmUri(lastfmArtist.getUrl());
            artistRepository.persist(artist);

            return artist;
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
