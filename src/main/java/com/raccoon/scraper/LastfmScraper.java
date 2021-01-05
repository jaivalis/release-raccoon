package com.raccoon.scraper;

import com.raccoon.config.LastFmConfig;
import de.umass.lastfm.Artist;
import de.umass.lastfm.Period;
import de.umass.lastfm.User;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Optional;

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
    public void scrapeTaste(String username, Optional<Integer> limit) {
        final Collection<Artist> topArtists = User.getTopArtists(username, Period.OVERALL, config.getApiKey());
        for (val artist : topArtists) {
            processArtist(artist);
        }
    }

    @Override
    public void processArtist(Object artist) {
//        assert artist instanceof
        log.info("{}", artist);

    }
}
