package com.raccoon.search;

import com.raccoon.entity.Artist;
import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.ArtistDtoProjector;
import com.raccoon.search.dto.ArtistSearchResponse;

import org.hibernate.search.mapper.orm.session.SearchSession;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SearchService {

    final SearchSession searchSession;
    final RaccoonLastfmApi lastfmApi;
    final ArtistDtoProjector artistDtoProjector;

    @Inject
    public SearchService(final SearchSession searchSession,
                         final RaccoonLastfmApi lastfmApi,
                         final ArtistDtoProjector artistDtoProjector) {
        this.searchSession = searchSession;
        this.lastfmApi = lastfmApi;
        this.artistDtoProjector = artistDtoProjector;
    }

    @Transactional
    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        if (Artist.count() > 0) {
            searchSession.massIndexer()
                    .startAndWait();
        }
    }

    /**
     * Search for an artist against the database and lastfm
     * @param pattern pattern to match artist name against
     * @param size search limit per resource (database and lastfm)
     * @return ArtistSearchResponse
     */
    public ArtistSearchResponse searchArtists(String pattern,
                                              Optional<Integer> size) {
        log.info("Searching for artist {}", pattern);

        var fromDb = searchDb(pattern, size);
        var fromLastfm = lastfmApi.searchArtist(pattern)
                .stream()
                .map(artistDtoProjector::project)
                .collect(Collectors.toSet());

        log.info("Found {} in db, {} in lastfm", fromDb.size(), fromLastfm.size());

        return ArtistSearchResponse.builder()
                .fromDb(fromDb)
                .fromLastfm(fromLastfm)
                .build();
    }

    @Transactional
    private Collection<ArtistDto> searchDb(String pattern,
                                           Optional<Integer> size) {
        return searchSession.search(Artist.class)
                .where(f ->
                        pattern == null || pattern.trim().isEmpty() ?
                                f.matchAll() :
                                f.simpleQueryString()
                                        .fields("name").matching(pattern)
                )
//                .sort(f -> f.field("name_sort"))
                .fetchHits(size.orElse(20))
                .stream()
                .map(artistDtoProjector::project)
                .collect(Collectors.toSet());
    }
}
