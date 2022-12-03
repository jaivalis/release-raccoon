package com.raccoon.search;

import com.raccoon.search.dto.SearchResultArtistDto;

import java.util.Collection;
import java.util.Optional;

public interface ArtistSearcher {

    /**
     * Used to identify the results in the dynamically generated ArtistSearchResponse dto
     * @return unique searcher id
     */
    String id();

    /**
     * Searcher confidence, used to rank the results returned to the raccoonUser
     */
    Double trustworthiness();

    /**
     * Search for an artist against available Searchers
     * @param pattern pattern to match artist name against
     * @param size search limit per resource (database and lastfm)
     * @return SearchResultArtistDto projection
     */
    Collection<SearchResultArtistDto> searchArtist(String pattern, Optional<Integer> size);

}
