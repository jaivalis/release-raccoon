package com.raccoon.search;

import com.raccoon.search.dto.ArtistSearchResponse;
import com.raccoon.search.impl.HibernateSearcher;
import com.raccoon.search.impl.LastfmSearcher;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SearchService {

    final HibernateSearcher hibernateSearcher;
    final LastfmSearcher lastfmSearcher;

    @Inject
    public SearchService(final HibernateSearcher hibernateSearcher,
                         final LastfmSearcher lastfmSearcher) {
        this.hibernateSearcher = hibernateSearcher;
        this.lastfmSearcher = lastfmSearcher;
    }

    /**
     * Search for an artist against available Searchers
     * @param pattern pattern to match artist name against
     * @param size search limit per resource (database and lastfm)
     * @return ArtistSearchResponse
     */
    public ArtistSearchResponse searchArtists(String pattern,
                                              Optional<Integer> size) {
        log.info("Searching for artist {}", pattern);

        var fromDb = hibernateSearcher.searchArtist(pattern, size);
        var fromLastfm = lastfmSearcher.searchArtist(pattern, size);
        log.info("Search hits: {} in db, {} in lastfm", fromDb.size(), fromLastfm.size());

        return ArtistSearchResponse.builder()
                .fromDb(fromDb)
                .fromLastfm(fromLastfm)
                .build();
    }

}
