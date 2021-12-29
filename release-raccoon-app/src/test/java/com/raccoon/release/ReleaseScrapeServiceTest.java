package com.raccoon.release;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.UserArtistRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseScrapeServiceTest {

    ReleaseScrapeService service;

    @Mock
    ReleaseScrapers releaseScrapersMock;
    @Mock
    UserArtistRepository userArtistRepositoryMock;

    @Captor
    ArgumentCaptor<List<Long>> captor;

    @BeforeEach
    void setUp() {
        service = new ReleaseScrapeService(
                releaseScrapersMock,
                userArtistRepositoryMock
        );
    }

    Set<Release> stubReleases(int limit) {
        return IntStream.range(0, limit)
                .mapToObj(this::stubRelease)
                .collect(Collectors.toSet());
    }

    Release stubRelease(int i) {
        var artist = new Artist();
        artist.setName("artist" + i);
        artist.setId((long) i);
        var artistRelease = new ArtistRelease();
        artistRelease.setArtist(artist);
        var release = new Release();
        release.setName("release" + i);
        release.setReleases(
                List.of(artistRelease)
        );
        artistRelease.setRelease(release);

        return release;
    }

    @Test
    void verifyInteractions() throws Exception {
        when(releaseScrapersMock.scrape()).thenReturn(Collections.emptySet());

        service.scrape();
        
        verify(releaseScrapersMock, times(1)).scrape();
    }

    @Test
    void scrapeReturnsAlbums() throws Exception {
        var releaseCount = 5;
        when(releaseScrapersMock.scrape())
                .thenReturn(stubReleases(releaseCount));

        service.scrape();

        verify(userArtistRepositoryMock, times(1)).markNewRelease(captor.capture());
        final var arg = captor.getValue();
        assertEquals(releaseCount, arg.size());
        // ids are given through `stubReleases` are lte `releaseCount`
        arg.forEach(artistId -> assertTrue(artistId < releaseCount));
    }
}