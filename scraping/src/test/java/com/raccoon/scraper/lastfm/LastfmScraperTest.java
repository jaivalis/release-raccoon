package com.raccoon.scraper.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistRepository;

import de.umass.lastfm.Period;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmScraperTest {

    LastfmScraper scraper;

    @Mock
    ArtistFactory artistFactoryMock;
    @Mock
    ArtistRepository artistRepositoryMock;
    @Mock
    RaccoonLastfmApi lastfmApiMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        scraper = new LastfmScraper(artistFactoryMock, artistRepositoryMock, lastfmApiMock);
    }

    private de.umass.lastfm.Artist stubArtist(int id) {
        var uri = "uri" + id;
        var name = "name" + id;
        var lastfmArtist = mock(de.umass.lastfm.Artist.class);
        when(lastfmArtist.getName()).thenReturn(name);
        when(lastfmArtist.getUrl()).thenReturn(uri);
        return lastfmArtist;
    }

    @Test
    @DisplayName("no play history")
    void scrapeTasteNoHistory() {
        var username = "username";

        // the API will return only one artist for every period requested.
        when(lastfmApiMock.getUserTopArtists(eq(username), any(Period.class)))
                .thenReturn(Collections.emptyList());

        final var taste = scraper.scrapeTaste(username, Optional.of(10));

        assertEquals(0, taste.size());
    }

    @Test
    @DisplayName("single artist played only")
    void scrapeTasteTest() {
        var username = "username";
        var uri = "test-uri";
        var name = "test-name";
        var lastfmArtist = mock(de.umass.lastfm.Artist.class);
        when(lastfmArtist.getName()).thenReturn(name);
        when(lastfmArtist.getUrl()).thenReturn(uri);

        Artist artistStub = new Artist();
        artistStub.setName(name);
        when(artistFactoryMock.getOrCreateArtist(name))
                .thenReturn(artistStub);

        // the API will return only one artist for every period requested.
        when(lastfmApiMock.getUserTopArtists(eq(username), any(Period.class)))
                .thenReturn(List.of(lastfmArtist));

        final var taste = scraper.scrapeTaste(username, Optional.of(10));

        assertEquals(1, taste.size());
    }

    @Test
    void processArtistSuccessful() {
        var uri = "test-uri";
        var name = "test-name";
        Artist artistStub = new Artist();
        artistStub.setName(name);
        var lastfmArtist = mock(de.umass.lastfm.Artist.class);
        when(lastfmArtist.getName()).thenReturn(name);
        when(lastfmArtist.getUrl()).thenReturn(uri);
        when(artistFactoryMock.getOrCreateArtist(name))
                .thenReturn(artistStub);

        final var artist = scraper.processArtist(lastfmArtist);

        assertEquals(name, artist.getName());
        assertEquals(uri, artist.getLastfmUri());
        verify(artistFactoryMock).getOrCreateArtist(name);
        verify(artistRepositoryMock).persist(any(Artist.class));
    }

    @Test
    @DisplayName("IllegalArgumentException if type is not de.umass.lastfm.Artist")
    void processArtistNonSpotifyType() {
        Object wrongType = new Object();

        assertThrows(IllegalArgumentException.class,
                () -> scraper.processArtist(wrongType));
    }
}