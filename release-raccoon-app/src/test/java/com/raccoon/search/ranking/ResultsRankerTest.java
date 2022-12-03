package com.raccoon.search.ranking;

import com.raccoon.Constants;
import com.raccoon.search.ArtistSearcher;
import com.raccoon.search.dto.SearchResultArtistDto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResultsRankerTest {

    ResultsRanker ranker = new ResultsRanker();

    class MockSearchService implements ArtistSearcher {
        Double trustworthiness;
        String id = "MockSearchService";

        MockSearchService(String id, Double trustworthiness) {
            this.id = id;
            this.trustworthiness = trustworthiness;
        }

        MockSearchService(Double trustworthiness) {
            this.trustworthiness = trustworthiness;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Double trustworthiness() {
            return trustworthiness;
        }

        @Override
        public Collection<SearchResultArtistDto> searchArtist(String pattern, Optional<Integer> size) {
            throw new UnsupportedOperationException("Should not be called");
        }
    }

    @Test
    void rankSearchResultsEmpty() {
        List<SearchResultArtistDto> artists = ranker.rankSearchResults(Collections.emptyMap(), Collections.emptyList());

        assertThat(artists).isEmpty();
    }

    @Test
    @DisplayName("rankSearchResults(): Same artist returned by two ArtistSearchers, should be merged on name")
    void rankSearchResultsMergeArtistsBasedOnName() {
        var artistName = "name";
        SearchResultArtistDto dto = SearchResultArtistDto.builder().name(artistName).build();
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.9), List.of(dto),
                new MockSearchService(.8), List.of(dto)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults(results, new ArrayList<>());

        assertThat(artists)
                .hasSize(1)
                .contains(dto);
    }

    @Test
    @DisplayName("rankSearchResults(): Hibernate results should be ignored in this step")
    void rankSearchResultsHibernateSkipped() {
        SearchResultArtistDto dto1 = SearchResultArtistDto.builder().name("artist one").build();
        SearchResultArtistDto dto2 = SearchResultArtistDto.builder().name("artist two").build();
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(Constants.HIBERNATE_SEARCHER_ID, 1.), List.of(dto1),
                new MockSearchService(.8), List.of(dto2)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults(results, new ArrayList<>());

        assertThat(artists)
                .hasSize(1)
                .contains(dto2);
    }

    @Test
    @DisplayName("rankSearchResults(): Highest trustworthiness results should come first")
    void rankSearchResultsSorting() {
        SearchResultArtistDto dto1 = SearchResultArtistDto.builder().name("artist one").build();
        SearchResultArtistDto dto2 = SearchResultArtistDto.builder().name("artist two").build();
        SearchResultArtistDto dto3 = SearchResultArtistDto.builder().name("artist three").build();
        SearchResultArtistDto dto4 = SearchResultArtistDto.builder().name("artist four").build();
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.2), List.of(dto1, dto2),
                new MockSearchService(.8), List.of(dto3, dto4)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults(results, new ArrayList<>());

        assertThat(artists)
                .hasSize(4)
                .contains(dto2);
        assertThat(artists.get(0)).isEqualTo(dto3);
        assertThat(artists.get(1)).isEqualTo(dto4);
        assertThat(artists.get(2)).isEqualTo(dto1);
        assertThat(artists.get(3)).isEqualTo(dto2);
    }

}