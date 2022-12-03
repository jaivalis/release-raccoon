package com.raccoon.search.impl;

import com.raccoon.Constants;
import com.raccoon.scraper.musicbrainz.MusicbrainzClient;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtist;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtistsResponse;
import com.raccoon.search.dto.SearchResultArtistDto;
import com.raccoon.search.dto.mapping.MusicbrainzArtistMapper;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicbrainzSearcherTest {

    MusicbrainzSearcher searcher;

    @Mock
    MusicbrainzClient mockMusicbrainzClient;
    @Mock
    MusicbrainzArtistMapper mockArtistMapper;

    @BeforeEach
    void setUp() {
        searcher = new MusicbrainzSearcher(mockMusicbrainzClient, mockArtistMapper);
    }

    @Test
    @DisplayName("id()")
    void searcherId() {
        assertEquals(Constants.MUSICBRAINZ_SEARCHER_ID, searcher.id());
    }

    @Test
    @DisplayName("trustworthiness()")
    void trustworthiness() {
        assertEquals(Constants.MUSICBRAINZ_SEARCHER_TRUSTWORTHINESS, searcher.trustworthiness());
    }

    @Test
    @DisplayName("searchArtists(): Some artists returned")
    void artistsFound() {
        String name = "name";
        Optional<Integer> size = Optional.of(10);
        MusicbrainzArtistsResponse response = new MusicbrainzArtistsResponse();
        response.setArtists(List.of(new MusicbrainzArtist()));
        response.setCount(1);
        when(mockMusicbrainzClient.searchArtistsByName(name, 10, 0)).thenReturn(response);

        searcher.searchArtist(name, size);

        verify(mockMusicbrainzClient, times(1)).searchArtistsByName(name, 10, 0);
        verify(mockArtistMapper, times(1)).toDto(any());
    }

    @Test
    @DisplayName("searchArtists(): No artists returned")
    void noArtistsFound() {
        String name = "name";
        Optional<Integer> size = Optional.of(10);
        MusicbrainzArtistsResponse response = new MusicbrainzArtistsResponse();
        response.setArtists(Collections.emptyList());
        response.setCount(0);
        when(mockMusicbrainzClient.searchArtistsByName(name, 10, 0)).thenReturn(response);

        searcher.searchArtist(name, size);

        verify(mockMusicbrainzClient, times(1)).searchArtistsByName(name, 10, 0);
        verify(mockArtistMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("searchArtists(): Exception thrown")
    void exceptionThrown() {
        String name = "name";
        Optional<Integer> size = Optional.of(10);
        when(mockMusicbrainzClient.searchArtistsByName(name, 10, 0)).thenThrow(RuntimeException.class);

        Collection<SearchResultArtistDto> dtos = searcher.searchArtist(name, size);

        assertThat(dtos).isEmpty();
        verify(mockMusicbrainzClient, times(1)).searchArtistsByName(name, 10, 0);
        verify(mockArtistMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("searchArtists(): Null response returned")
    void nullResponse() {
        String name = "name";
        Optional<Integer> size = Optional.of(10);
        when(mockMusicbrainzClient.searchArtistsByName(name, 10, 0)).thenReturn(null);

        searcher.searchArtist(name, size);

        verify(mockMusicbrainzClient, times(1)).searchArtistsByName(name, 10, 0);
        verify(mockArtistMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("searchArtists(): Null response returned")
    void nullArtists() {
        String name = "name";
        Optional<Integer> size = Optional.of(10);
        MusicbrainzArtistsResponse response = new MusicbrainzArtistsResponse();
        response.setArtists(null);
        response.setCount(0);
        when(mockMusicbrainzClient.searchArtistsByName(name, 10, 0)).thenReturn(response);

        searcher.searchArtist(name, size);

        verify(mockMusicbrainzClient, times(1)).searchArtistsByName(name, 10, 0);
        verify(mockArtistMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("searchArtists(): should limit")
    void searchArtistsShouldLimit() {
        var name = "name";
        var size = Optional.of(10);
        MusicbrainzArtistsResponse twentyArtistsResponse = new MusicbrainzArtistsResponse();
        twentyArtistsResponse.setArtists(
                IntStream.range(0, 20)
                        .mapToObj(i -> Mockito.mock(MusicbrainzArtist.class))
                        .toList()
        );
        twentyArtistsResponse.setCount(20);
        when(mockMusicbrainzClient.searchArtistsByName(name, size.get(), 0)).thenReturn(twentyArtistsResponse);

        searcher.searchArtist(name, size);

        verify(mockArtistMapper, times(size.get())).toDto(any());
    }

    @Test
    @DisplayName("searchArtists() should not limit")
    void searchArtistsShouldNotLimit() {
        var name = "name";
        MusicbrainzArtistsResponse twentyArtistsResponse = new MusicbrainzArtistsResponse();
        twentyArtistsResponse.setArtists(
                IntStream.range(0, 20)
                        .mapToObj(i -> Mockito.mock(MusicbrainzArtist.class))
                        .toList()
        );
        twentyArtistsResponse.setCount(20);
        Optional<Integer> empty = Optional.empty();
        when(mockMusicbrainzClient.searchArtistsByName(name, 20, 0)).thenReturn(twentyArtistsResponse);

        searcher.searchArtist(name, empty);

        verify(mockArtistMapper, times(20)).toDto(any());
    }

}