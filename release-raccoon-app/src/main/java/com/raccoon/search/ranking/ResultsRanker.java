package com.raccoon.search.ranking;

import com.raccoon.Constants;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.SearchResultArtistDto;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResultsRanker {

    /**
     * Ranks results by searcher trustworthiness. Merges artists with same name.
     * @param searchResultsPerSource search results per search source
     * @param rankedResults artists that might have been appended to from hibernate searcher
     * @return ordered artist search results
     */
    public List<SearchResultArtistDto> rankSearchResults(final Map<ArtistSearcher, Collection<SearchResultArtistDto>> searchResultsPerSource,
                                                         final List<SearchResultArtistDto> rankedResults) {
        List<ArtistSearcher> searchersSortedOnTrustworthiness =
                searchResultsPerSource.keySet().stream().sorted(
                        Comparator.comparing(ArtistSearcher::trustworthiness).reversed()
                ).toList();

        for (ArtistSearcher searcher : searchersSortedOnTrustworthiness) {
            if (Constants.HIBERNATE_SEARCHER_ID.equals(searcher.id())) {
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

        return rankedResults;
    }

}
