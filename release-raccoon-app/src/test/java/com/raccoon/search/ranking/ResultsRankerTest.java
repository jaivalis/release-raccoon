package com.raccoon.search.ranking;

import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.ArtistDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResultsRankerTest {

    ResultsRanker ranker = new ResultsRanker();

    @Test
    void rankSearchResultsEmpty() {
        List<ArtistDto> artists = ranker.rankSearchResults(Collections.emptyMap(), Collections.emptyList());

        assertThat(artists).isEmpty();
    }

    @Test
    @DisplayName("rankSearchResults(): Same artist returned by two ArtistSearchers, should be merged on name")
    void rankSearchResultsMergeArtistsBasedOnName() {
        var artistName = "name";
        ArtistDto dto = ArtistDto.builder().name(artistName).build();
        Map<ArtistSearcher, Collection<ArtistDto>> results = Map.of(
                new MockSearchService(.9), List.of(dto),
                new MockSearchService(.8), List.of(dto)
        );

        List<ArtistDto> artists = ranker.rankSearchResults(results, new ArrayList<>());

        assertThat(artists)
                .hasSize(1)
                .contains(dto);
    }


    class MockSearchService implements ArtistSearcher {
        Double trustworthiness;

        MockSearchService(Double trustworthiness) {
            this.trustworthiness = trustworthiness;
        }

        @Override
        public String id() {
            return "MockSearchService";
        }

        @Override
        public Double trustworthiness() {
            return trustworthiness;
        }

        @Override
        public Collection<ArtistDto> searchArtist(String pattern, Optional<Integer> size) {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

}