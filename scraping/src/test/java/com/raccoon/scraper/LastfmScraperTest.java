package com.raccoon.scraper;

import com.raccoon.entity.Artist;
import com.raccoon.entity.factory.ArtistFactory;
import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.scraper.config.LastFmConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    LastFmConfig configMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        scraper = new LastfmScraper(configMock, artistFactoryMock, artistRepositoryMock);
    }

    @Test
    void processArtistSuccessful() {
        var uri = "test-uri";
        var name = "test-name";
        Artist artistStub = new Artist();
        artistStub.setName(name);
        de.umass.lastfm.Artist lastfmArtist = mock(de.umass.lastfm.Artist.class);
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