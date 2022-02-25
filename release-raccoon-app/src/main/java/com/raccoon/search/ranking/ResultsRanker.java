package com.raccoon.search.ranking;

import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.impl.HibernateSearcher;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResultsRanker {

    public ResultsRanker() {}

    public List<ArtistDto> rankSearchResults(Map<ArtistSearcher, Collection<ArtistDto>> searchResultsPerSource,
                                             List<ArtistDto> rankedResults) {
        List<ArtistSearcher> searchersSortedOnTrustworthiness = searchResultsPerSource.keySet().stream().sorted(
                Comparator.comparing(ArtistSearcher::trustworthiness)
        ).toList();

        for (ArtistSearcher searcher : searchersSortedOnTrustworthiness) {
            if (searcher instanceof HibernateSearcher) {
                // Hibernate results have already been ranked top of the list
                continue;
            }

            Collection<ArtistDto> searcherHits = searchResultsPerSource.get(searcher);

            for (ArtistDto artistDto : searcherHits) {
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
