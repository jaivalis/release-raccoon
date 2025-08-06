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
import java.util.Comparator;
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
    void rankSearchResultsEmpty_should_returnEmpty_when_noResults() {
        List<SearchResultArtistDto> artists = ranker.rankSearchResults("query", Collections.emptyMap(), Collections.emptyList());

        assertThat(artists).isEmpty();
    }

    @Test
    @DisplayName("rankSearchResults(): Same artist returned by two ArtistSearchers, should be merged on name")
    void rankSearchResults_should_mergeArtists_when_sameNameFromMultipleSources() {
        var artistName = "name";
        SearchResultArtistDto dto = SearchResultArtistDto.builder().name(artistName).build();
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.9), List.of(dto),
                new MockSearchService(.8), List.of(dto)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("query", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(1)
                .contains(dto);
    }

    @Test
    @DisplayName("rankSearchResults(): Hibernate results should be ignored in this step")
    void rankSearchResults_should_skipHibernateResults_when_alreadyRanked() {
        SearchResultArtistDto dto1 = SearchResultArtistDto.builder().name("artist one").build();
        SearchResultArtistDto dto2 = SearchResultArtistDto.builder().name("artist two").build();
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(Constants.HIBERNATE_SEARCHER_ID, 1.), List.of(dto1),
                new MockSearchService(.8), List.of(dto2)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("query", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(1)
                .contains(dto2);
    }

    @Test
    @DisplayName("rankSearchResults(): Highest trustworthiness results should come first")
    void rankSearchResults_should_sortByTrustworthiness_when_queryNotInNames() {
        SearchResultArtistDto dto1 = SearchResultArtistDto.builder().name("artist one").build();
        SearchResultArtistDto dto2 = SearchResultArtistDto.builder().name("artist two").build();
        SearchResultArtistDto dto3 = SearchResultArtistDto.builder().name("artist three").build();
        SearchResultArtistDto dto4 = SearchResultArtistDto.builder().name("artist four").build();
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.2), List.of(dto1, dto2),
                new MockSearchService(.8), List.of(dto3, dto4)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("zebra", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(4)
                .contains(dto2);
        assertThat(artists.get(0)).isEqualTo(dto3);
        assertThat(artists.get(1)).isEqualTo(dto4);
        assertThat(artists.get(2)).isEqualTo(dto1);
        assertThat(artists.get(3)).isEqualTo(dto2);
    }

    @Test
    @DisplayName("rankSearchResults(): Results containing query should be prioritized")
    void rankSearchResults_should_prioritizeMatchingResults_when_queryInArtistName() {
        SearchResultArtistDto matchingDto1 = SearchResultArtistDto.builder().name("Pink Floyd").build();
        SearchResultArtistDto matchingDto2 = SearchResultArtistDto.builder().name("Pink").build();
        SearchResultArtistDto nonMatchingDto1 = SearchResultArtistDto.builder().name("Led Zeppelin").build();
        SearchResultArtistDto nonMatchingDto2 = SearchResultArtistDto.builder().name("The Beatles").build();
        
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.5), List.of(nonMatchingDto1, matchingDto1),
                new MockSearchService(.8), List.of(nonMatchingDto2, matchingDto2)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("pink", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(4);
        // Results containing "pink" should come first
        assertThat(artists.get(0).getName()).containsIgnoringCase("pink");
        assertThat(artists.get(1).getName()).containsIgnoringCase("pink");
        // Non-matching results should come after
        assertThat(artists.get(2).getName()).doesNotContainIgnoringCase("pink");
        assertThat(artists.get(3).getName()).doesNotContainIgnoringCase("pink");
    }

    @Test
    @DisplayName("rankSearchResults(): Case-insensitive query matching")
    void rankSearchResults_should_matchCaseInsensitive_when_differentCasing() {
        SearchResultArtistDto upperCaseMatch = SearchResultArtistDto.builder().name("RADIOHEAD").build();
        SearchResultArtistDto lowerCaseMatch = SearchResultArtistDto.builder().name("radiohead").build();
        SearchResultArtistDto mixedCaseMatch = SearchResultArtistDto.builder().name("RadioHead").build();
        SearchResultArtistDto nonMatch = SearchResultArtistDto.builder().name("Coldplay").build();
        
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.5), List.of(upperCaseMatch, nonMatch),
                new MockSearchService(.8), List.of(lowerCaseMatch, mixedCaseMatch)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("RADIOHEAD", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(4);
        // All Radiohead variants should come first (regardless of case)
        assertThat(artists.subList(0, 3))
                .extracting(SearchResultArtistDto::getName)
                .allMatch(name -> name.toLowerCase().contains("radiohead"));
        assertThat(artists.get(3).getName()).isEqualTo("Coldplay");
    }

    @Test
    @DisplayName("rankSearchResults(): Partial query matches should be prioritized")
    void rankSearchResults_should_prioritizePartialMatches_when_queryIsSubstring() {
        SearchResultArtistDto fullMatch = SearchResultArtistDto.builder().name("The Rolling Stones").build();
        SearchResultArtistDto partialMatch = SearchResultArtistDto.builder().name("Stone Temple Pilots").build();
        SearchResultArtistDto noMatch = SearchResultArtistDto.builder().name("Pearl Jam").build();
        
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.5), List.of(noMatch, fullMatch, partialMatch)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("stone", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(3);
        // Both matches containing "stone" should come first
        assertThat(artists.get(0).getName()).containsIgnoringCase("stone");
        assertThat(artists.get(1).getName()).containsIgnoringCase("stone");
        assertThat(artists.get(2).getName()).isEqualTo("Pearl Jam");
    }

    @Test
    @DisplayName("rankSearchResults(): Exact matches should be prioritized over partial matches")
    void rankSearchResults_should_prioritizeExactMatches_when_bothExactAndPartialMatchesExist() {
        // This test simulates the IDLES search scenario
        SearchResultArtistDto exactMatch = SearchResultArtistDto.builder().name("IDLES").build();
        SearchResultArtistDto partialMatch1 = SearchResultArtistDto.builder().name("IDLES Lidless Eye").build();
        SearchResultArtistDto partialMatch2 = SearchResultArtistDto.builder().name("Lidless Sound").build();
        SearchResultArtistDto partialMatch3 = SearchResultArtistDto.builder().name("AcidLess").build();
        SearchResultArtistDto partialMatch4 = SearchResultArtistDto.builder().name("Voidless").build();
        SearchResultArtistDto partialMatch5 = SearchResultArtistDto.builder().name("Emma & the Idles").build();
        SearchResultArtistDto partialMatch6 = SearchResultArtistDto.builder().name("Bluegrass Idles").build();
        
        Map<ArtistSearcher, Collection<SearchResultArtistDto>> results = Map.of(
                new MockSearchService(.5), List.of(partialMatch1, partialMatch2, partialMatch3, partialMatch4),
                new MockSearchService(.8), List.of(partialMatch5, partialMatch6, exactMatch)
        );

        List<SearchResultArtistDto> artists = ranker.rankSearchResults("idles", results, new ArrayList<>());

        assertThat(artists)
                .hasSize(7);
        // Exact match should be first
        assertThat(artists.getFirst().getName()).isEqualTo("IDLES");
        // Then other matches containing "IDLES" (case-insensitive)
        assertThat(artists.subList(1, 7))
                .extracting(SearchResultArtistDto::getName)
                .containsExactlyInAnyOrder(
                    "IDLES Lidless Eye",
                    "Lidless Sound",
                    "AcidLess", 
                    "Voidless",
                    "Emma & the Idles",
                    "Bluegrass Idles"
                );
    }

    @Test
    @DisplayName("createSearchResultComparator(): Should return negative when first is exact match and second is not")
    void createSearchResultComparator_should_returnNegative_when_firstIsExactMatch() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("IDLES");
        
        SearchResultArtistDto exactMatch = SearchResultArtistDto.builder().name("IDLES").build();
        SearchResultArtistDto partialMatch = SearchResultArtistDto.builder().name("IDLES Lidless Eye").build();
        
        int result = comparator.compare(exactMatch, partialMatch);
        
        assertThat(result).isNegative();
    }

    @Test
    @DisplayName("createSearchResultComparator(): Should return positive when second is exact match and first is not")
    void createSearchResultComparator_should_returnPositive_when_secondIsExactMatch() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("IDLES");
        
        SearchResultArtistDto partialMatch = SearchResultArtistDto.builder().name("IDLES Lidless Eye").build();
        SearchResultArtistDto exactMatch = SearchResultArtistDto.builder().name("IDLES").build();
        
        int result = comparator.compare(partialMatch, exactMatch);
        
        assertThat(result).isPositive();
    }

    @Test
    @DisplayName("createSearchResultComparator(): Should return zero when both are exact matches")
    void createSearchResultComparator_should_returnZero_when_bothExactMatches() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("radiohead");
        
        SearchResultArtistDto exactMatch1 = SearchResultArtistDto.builder().name("Radiohead").build();
        SearchResultArtistDto exactMatch2 = SearchResultArtistDto.builder().name("RADIOHEAD").build();
        
        int result = comparator.compare(exactMatch1, exactMatch2);
        
        assertThat(result).isZero();
    }

    @Test
    @DisplayName("createSearchResultComparator(): Should prioritize partial match over non-match")
    void createSearchResultComparator_should_prioritizePartialMatch_when_comparedToNonMatch() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("stone");
        
        SearchResultArtistDto partialMatch = SearchResultArtistDto.builder().name("The Rolling Stones").build();
        SearchResultArtistDto nonMatch = SearchResultArtistDto.builder().name("The Beatles").build();
        
        int result = comparator.compare(partialMatch, nonMatch);
        
        assertThat(result).isNegative();
    }

    @Test
    @DisplayName("createSearchResultComparator(): Should return zero when both are partial matches")
    void createSearchResultComparator_should_returnZero_when_bothPartialMatches() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("stone");
        
        SearchResultArtistDto partialMatch1 = SearchResultArtistDto.builder().name("The Rolling Stones").build();
        SearchResultArtistDto partialMatch2 = SearchResultArtistDto.builder().name("Stone Temple Pilots").build();
        
        int result = comparator.compare(partialMatch1, partialMatch2);
        
        assertThat(result).isZero();
    }

    @Test
    @DisplayName("createSearchResultComparator(): Should return zero when neither match")
    void createSearchResultComparator_should_returnZero_when_neitherMatch() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("queen");
        
        SearchResultArtistDto nonMatch1 = SearchResultArtistDto.builder().name("The Beatles").build();
        SearchResultArtistDto nonMatch2 = SearchResultArtistDto.builder().name("Led Zeppelin").build();
        
        int result = comparator.compare(nonMatch1, nonMatch2);
        
        assertThat(result).isZero();
    }

    @Test
    @DisplayName("createSearchResultComparator(): Case-insensitive exact match detection")
    void createSearchResultComparator_should_detectExactMatch_when_differentCase() {
        Comparator<SearchResultArtistDto> comparator = ranker.createSearchResultComparator("pink floyd");
        
        SearchResultArtistDto upperCase = SearchResultArtistDto.builder().name("PINK FLOYD").build();
        SearchResultArtistDto mixedCase = SearchResultArtistDto.builder().name("Pink Floyd").build();
        SearchResultArtistDto partialMatch = SearchResultArtistDto.builder().name("Pink Floyd Tribute").build();
        
        // Both exact matches should be equal
        assertThat(comparator.compare(upperCase, mixedCase)).isZero();
        
        // Exact matches should come before partial matches
        assertThat(comparator.compare(upperCase, partialMatch)).isNegative();
        assertThat(comparator.compare(mixedCase, partialMatch)).isNegative();
    }

}