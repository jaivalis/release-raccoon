package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.scraper.lastfm.RaccoonLastfmApi;
import com.raccoon.search.dto.mapping.LastFmArtistMapper;

import de.umass.lastfm.Artist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmSearcherTest {

    LastfmSearcher searcher;

    @Mock
    RaccoonLastfmApi mockLastfmApi;
    @Mock
    LastFmArtistMapper mockLastFmArtistMapper;

    @BeforeEach
    void setUp() {
        searcher = new LastfmSearcher(mockLastfmApi, mockLastFmArtistMapper);
    }

    @Test
    @DisplayName("id()")
    void searcherId() {
        assertEquals(Constants.LASTFM_SEARCHER_ID, searcher.id());
    }

    @Test
    @DisplayName("trustworthiness()")
    void trustworthiness() {
        assertEquals(Constants.LASTFM_SEARCHER_TRUSTWORTHINESS, searcher.trustworthiness());
    }

    @Test
    @DisplayName("Some artists returned")
    void artistsFound() {
        String pattern = "pattern";
        Optional<Integer> size = Optional.of(10);

        List<Artist> artists = IntStream.range(1, 11)
                .mapToObj(i -> Mockito.mock(Artist.class))
                .toList();
        when(mockLastfmApi.searchArtist(pattern)).thenReturn(artists);

        searcher.searchArtist(pattern, size);

        verify(mockLastfmApi, times(1)).searchArtist(pattern);
        verify(mockLastFmArtistMapper, times(artists.size())).toDto(any(Artist.class));
    }

    @Test
    @DisplayName("No artists returned")
    void noArtistsFound() {
        String pattern = "pattern";
        Optional<Integer> size = Optional.of(10);
        when(mockLastfmApi.searchArtist(pattern)).thenReturn(Collections.emptyList());

        searcher.searchArtist(pattern, size);

        verify(mockLastfmApi, times(1)).searchArtist(pattern);
        verify(mockLastFmArtistMapper, never()).toDto(any(Artist.class));
    }


    @Test
    @DisplayName("searchArtists() should limit")
    void searchArtistsShouldLimit() {
        var pattern = "pattern";
        var size = Optional.of(10);
        Collection<Artist> twentyArtists = IntStream.range(0, 20)
                .mapToObj(i -> Mockito.mock(Artist.class))
                .toList();
        when(mockLastfmApi.searchArtist(pattern)).thenReturn(twentyArtists);

        searcher.searchArtist(pattern, size);

        verify(mockLastFmArtistMapper, times(size.get())).toDto(any(Artist.class));
    }

    @Test
    @DisplayName("searchArtists() should not limit")
    void searchArtistsShouldNotLimit() {
        var pattern = "pattern";
        Collection<Artist> twentyArtists = IntStream.range(0, 20)
                .mapToObj(i -> Mockito.mock(Artist.class))
                .toList();
        Optional<Integer> empty = Optional.empty();
        when(mockLastfmApi.searchArtist(pattern)).thenReturn(twentyArtists);

        searcher.searchArtist(pattern, empty);

        verify(mockLastFmArtistMapper, times(20)).toDto(any(Artist.class));
    }

}
