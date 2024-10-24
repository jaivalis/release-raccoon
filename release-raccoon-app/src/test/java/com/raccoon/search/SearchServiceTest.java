package com.raccoon.search;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.search.dto.mapping.ArtistSearchResponse;
import com.raccoon.search.impl.HibernateSearcher;
import com.raccoon.search.impl.LastfmSearcher;
import com.raccoon.search.ranking.ResultsRanker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;

import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    SearchService service;

    @Mock
    HibernateSearcher mockHibernateSearcher;
    @Mock
    LastfmSearcher mockLastfmSearcher;
    @Mock
    Instance<ArtistSearcher> searchers;
    ResultsRanker ranker = new ResultsRanker();
    @Mock
    UserRepository userRepository;
    @Mock
    UserArtistRepository userArtistRepository;

    @BeforeEach
    void setUp() {
        openMocks(this);
        when(searchers.stream()).thenReturn(Stream.of(mockHibernateSearcher, mockLastfmSearcher));
        when(mockLastfmSearcher.id()).thenCallRealMethod();
        when(mockLastfmSearcher.trustworthiness()).thenCallRealMethod();
        when(mockHibernateSearcher.id()).thenCallRealMethod();
        when(mockHibernateSearcher.trustworthiness()).thenCallRealMethod();

        service = new SearchService(searchers, ranker, userRepository, userArtistRepository);
    }

    @Test
    @DisplayName("searchArtists Invokes search for all searchers")
    void searchArtists() {
        var email = "email";
        var pattern = "pattern";
        var size = Optional.of(10);
        when(mockHibernateSearcher.searchArtist(any(), any())).thenReturn(Collections.emptyList());
        when(mockLastfmSearcher.searchArtist(any(), any())).thenReturn(Collections.emptyList());

        service.searchArtists(email, pattern, size);

        verify(mockHibernateSearcher, times(1)).searchArtist(pattern, size);
        verify(mockLastfmSearcher, times(1)).searchArtist(pattern, size);
    }

    @Test
    @DisplayName("searchArtists Populates the result as expected")
    void searchArtistsReturns() {
        var pattern = "pattern";
        var size = Optional.of(10);
        SearchResultArtistDto stubArtist1 = SearchResultArtistDto.builder().name("hibernate").id(3L).build();
        SearchResultArtistDto stubArtist2 = SearchResultArtistDto.builder().name("hibernate2 followed by raccoonUser should appear first").id(9L).build();
        SearchResultArtistDto stubArtist3 = SearchResultArtistDto.builder().name("lastfm").build();
        when(mockHibernateSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist1, stubArtist2));
        when(mockLastfmSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist3));

        var stubUser = new RaccoonUser();
        stubUser.id = 1L;
        var stubArtist = new Artist();
        // The artist that we want returned first (since already followed)
        stubArtist.id = stubArtist2.getId();
        when(userRepository.findByEmail(any())).thenReturn(stubUser);
        var stubUserArtist = new UserArtist();
        stubUserArtist.setUser(stubUser);
        stubUserArtist.setArtist(stubArtist);
        when(userArtistRepository.findByUserIdAndArtistIds(stubUser.id, List.of(stubArtist1.getId(), stubArtist2.getId())))
                .thenReturn(List.of(
                        stubUserArtist
                ));

        ArtistSearchResponse response = service.searchArtists("email", pattern, size);

        assertThat(response.getArtists()).hasSize(3);
        assertThat(response.getArtists())
                .containsAll(List.of(stubArtist1, stubArtist2, stubArtist3));
        for (var dto : response.getArtists()) {
            if ("hibernate2 followed by raccoonUser should appear first".equals(dto.getName())) {
                assertTrue(dto.isFollowedByUser());
            } else {
                assertFalse(dto.isFollowedByUser());
            }
        }
    }

    @Test
    @DisplayName("searchArtists should merge same name artists")
    void searchArtists_should_mergeResultsFromDifferentSearchers() {
        var pattern = "pattern";
        var size = Optional.of(10);
        String commonName = "same-name";
        SearchResultArtistDto stubArtist1 = SearchResultArtistDto.builder()
                .name(commonName)
                .spotifyUri("uri1")
                .id(9L)
                .build();
        SearchResultArtistDto stubArtist2 = SearchResultArtistDto.builder()
                .name(commonName)
                .musicbrainzId("musicbrainzId1")
                .build();
        when(mockHibernateSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist1));
        when(mockLastfmSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist2));

        var stubUser = new RaccoonUser();
        stubUser.id = 1L;
        var stubArtist = new Artist();
        // The artist that we want returned first (since already followed)
        stubArtist.id = stubArtist2.getId();
        when(userRepository.findByEmail(any())).thenReturn(stubUser);
        var stubUserArtist = new UserArtist();
        stubUserArtist.setUser(stubUser);
        stubUserArtist.setArtist(stubArtist);

        ArtistSearchResponse response = service.searchArtists("email", pattern, size);

        assertThat(response.getArtists()).hasSize(1);
        assertThat(response.getArtists().get(0))
                .extracting("id", "name", "spotifyUri", "musicbrainzId")
                .containsOnly(9L, commonName, "uri1", "musicbrainzId1");
    }

}
