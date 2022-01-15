package com.raccoon.search;

import com.raccoon.search.dto.ArtistDto;
import com.raccoon.search.dto.ArtistSearchResponse;
import com.raccoon.search.impl.HibernateSearcher;
import com.raccoon.search.impl.LastfmSearcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import static com.raccoon.Constants.HIBERNATE_SEARCHER_ID;
import static com.raccoon.Constants.LASTFM_SEARCHER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeEach
    void setUp() {
        openMocks(this);

        when(searchers.stream()).thenReturn(Stream.of(mockHibernateSearcher, mockLastfmSearcher));
        service = new SearchService(searchers);
    }

    @Test
    @DisplayName("searchArtists(): Invokes search for all searchers")
    void searchArtists() {
        var pattern = "pattern";
        var size = Optional.of(10);

        service.searchArtists(pattern, size);

        verify(mockHibernateSearcher, times(1)).searchArtist(pattern, size);
        verify(mockLastfmSearcher, times(1)).searchArtist(pattern, size);
    }

    @Test
    @DisplayName("searchArtists(): Populates the result as expected")
    void searchArtistsReturns() {
        var pattern = "pattern";
        var size = Optional.of(10);
        ArtistDto stubArtist1 = ArtistDto.builder().name("hibernate").build();
        ArtistDto stubArtist2 = ArtistDto.builder().name("lastfm").build();
        when(mockHibernateSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist1));
        when(mockLastfmSearcher.searchArtist(pattern, size)).thenReturn(List.of(stubArtist2));
        when(mockLastfmSearcher.getSearcherId()).thenReturn(LASTFM_SEARCHER_ID);
        when(mockHibernateSearcher.getSearcherId()).thenReturn(HIBERNATE_SEARCHER_ID);

        ArtistSearchResponse response = service.searchArtists(pattern, size);

        assertEquals(1, response.getArtistsPerResource().get(HIBERNATE_SEARCHER_ID).size());
        assertEquals(1, response.getArtistsPerResource().get(LASTFM_SEARCHER_ID).size());
        assertEquals(stubArtist1, response.getArtistsPerResource().get(HIBERNATE_SEARCHER_ID).iterator().next());
        assertEquals(stubArtist2, response.getArtistsPerResource().get(LASTFM_SEARCHER_ID).iterator().next());
    }

}
