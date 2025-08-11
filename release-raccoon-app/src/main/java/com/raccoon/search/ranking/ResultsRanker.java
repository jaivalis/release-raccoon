package com.raccoon.search.ranking;

import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.SearchResultArtistDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.HIBERNATE_SEARCHER_ID;
import static java.util.Objects.isNull;

@ApplicationScoped
@Slf4j
public class ResultsRanker {

    /**
     * Ranks results by searcher trustworthiness. Merges artists with same name.
     * Results containing the search query are prioritized.
     * @param query the search query
     * @param searchResultsPerSource search results per search source
     * @param rankedResults artists that might have been appended to from hibernate searcher
     * @return ordered artist search results
     */
    public List<SearchResultArtistDto> rankSearchResults(final String query,
                                                         final Map<ArtistSearcher, Collection<SearchResultArtistDto>> searchResultsPerSource,
                                                         final List<SearchResultArtistDto> rankedResults) {
        log.info("Ranking: {}", rankedResults);
        List<ArtistSearcher> searchersSortedOnTrustworthiness =
                searchResultsPerSource.keySet().stream()
                        .sorted(
                                Comparator.comparing(ArtistSearcher::trustworthiness).reversed()
                        ).toList();

        for (ArtistSearcher searcher : searchersSortedOnTrustworthiness) {
            if (HIBERNATE_SEARCHER_ID.equals(searcher.id())) {
                // Hibernate results have already been ranked top of the list
                continue;
            }

            Collection<SearchResultArtistDto> searcherHits = searchResultsPerSource.get(searcher);

            for (SearchResultArtistDto artistDto : searcherHits) {
                if (rankedResults.contains(artistDto)) {
                    rankedResults.get(rankedResults.indexOf(artistDto)).merge(artistDto);
                } else {
                    rankedResults.add(artistDto);
                }
            }
        }

        // Sort results to prioritize those containing the search query
        return prioritizeResultsContainingQuery(query, rankedResults);
    }

    /**
     * Prioritizes results that contain the search query in their name.
     * Exact matches come first, then partial matches, then non-matches.
     * Case-insensitive matching is used.
     * @param query the search query
     * @param results the list of results to sort
     * @return sorted list with query-matching results first
     */
    private List<SearchResultArtistDto> prioritizeResultsContainingQuery(final String query, final List<SearchResultArtistDto> results) {
        if (isNull(query) || query.isBlank() || results.isEmpty()) {
            return results;
        }
        
        return results.stream()
                .sorted(createSearchResultComparator(query))
                .toList();
    }
    
    /**
     * Creates a comparator that prioritizes search results based on query matching.
     * Order of priority:
     * 1. Exact matches (case-insensitive)
     * 2. Partial matches containing the query
     * 3. Non-matches
     * 
     * @param query the search query to match against
     * @return comparator for SearchResultArtistDto
     */
    Comparator<SearchResultArtistDto> createSearchResultComparator(final String query) {
        String lowerCaseQuery = query.toLowerCase();
        
        return (a, b) -> {
            String aNameLower = a.getName().toLowerCase();
            String bNameLower = b.getName().toLowerCase();

            boolean aExactMatch = aNameLower.equals(lowerCaseQuery);
            boolean bExactMatch = bNameLower.equals(lowerCaseQuery);
            boolean aContainsQuery = aNameLower.contains(lowerCaseQuery);
            boolean bContainsQuery = bNameLower.contains(lowerCaseQuery);
            
            // First priority: exact matches
            if (aExactMatch && !bExactMatch) {
                return -1;
            } else if (!aExactMatch && bExactMatch) {
                return 1;
            }
            
            // Second priority: partial matches
            if (aContainsQuery && !bContainsQuery) {
                return -1;
            } else if (!aContainsQuery && bContainsQuery) {
                return 1;
            }
            
            return 0;
        };
    }

}
