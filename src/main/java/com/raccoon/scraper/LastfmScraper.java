package com.raccoon.scraper;

import com.raccoon.config.LastFmConfig;
import com.raccoon.entity.Artist;
import com.raccoon.entity.factory.ArtistFactory;
import de.umass.lastfm.Period;
import de.umass.lastfm.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class LastfmScraper implements TasteScraper {

    @Inject
    LastFmConfig config;

    @Inject
    public LastfmScraper(LastFmConfig config) {
        this.config = config;
        log.info("Config: {}", config);
    }

    @Override
    public Collection<MutablePair<Artist, Float>> scrapeTaste(String username, Optional<Integer> limit) {
        final Collection<de.umass.lastfm.Artist> topArtists = User.getTopArtists(username, Period.OVERALL, config.getApiKey());

        return topArtists.stream()
                .map(artistObj -> MutablePair.of(processArtist(artistObj), (float) artistObj.getPlaycount()))
                .collect(Collectors.toList());
    }

    @Override
    public com.raccoon.entity.Artist processArtist(Object artistObj) {
        log.info("{}", artistObj);
        if (artistObj instanceof de.umass.lastfm.Artist) {
            de.umass.lastfm.Artist lastfmArtist = (de.umass.lastfm.Artist) artistObj;

            return ArtistFactory.getOrCreateArtist(lastfmArtist.getName());
        }
        throw new IllegalStateException("Got an object type that is not supported.");
    }
}
