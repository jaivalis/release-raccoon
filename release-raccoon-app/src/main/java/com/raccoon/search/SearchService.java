package com.raccoon.search;

import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.ArtistSearchResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class SearchService {

    final List<ArtistSearcher> searchers;

    @Inject
    public SearchService(final Instance<ArtistSearcher> searchers) {
        this.searchers = searchers.stream().toList();
        log.info("Found {} artist searchers in classpath", this.searchers.size());
    }

    /**
     * Search for an artist against available Searchers
     * @param pattern pattern to match artist name against
     * @param size search limit per resource (database and lastfm)
     * @return ArtistSearchResponse
     */
    public ArtistSearchResponse searchArtists(final String pattern,
                                              final Optional<Integer> size) {
        log.info("Searching for artist {}", pattern);

        Map<String, Collection<ArtistDto>> artistsPerResource = new HashMap<>();

        searchers.parallelStream().forEach(
                searcher -> {
                    var searcherId = searcher.getSearcherId();
                    var results = searcher.searchArtist(pattern, size);
                    log.info("Search hits, source `{}`: {} ", searcherId, results);

                    artistsPerResource.put(searcherId, results);
                }
        );

        return ArtistSearchResponse.builder()
                .artistsPerResource(artistsPerResource)
                .build();
    }

}
