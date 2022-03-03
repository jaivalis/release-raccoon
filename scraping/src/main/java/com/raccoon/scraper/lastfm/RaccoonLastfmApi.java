package com.raccoon.scraper.lastfm;

import com.raccoon.scraper.config.LastFmConfig;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Period;
import de.umass.lastfm.User;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

/**
 * Holds logic for querying lastfm
 */
@Slf4j
@ApplicationScoped
public class RaccoonLastfmApi {

    final String apiKey;

    @Inject
    RaccoonLastfmApi(final LastFmConfig config) {
        apiKey = config.apiKey();
    }

    /**
     * Returns a collection of lastfm Artist objects for a given period of time for a user.
     * @param username lastfm username
     * @param period period of time to query for
     * @return
     */
    public Collection<Artist> getUserTopArtists(String username, Period period) {
        return User.getTopArtists(username, period, apiKey);
    }

    public Collection<Artist> searchArtist(String pattern) {
        return Artist.search(pattern, apiKey);
    }

}
