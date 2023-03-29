package com.raccoon.release;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.Scrape;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.release.dto.ReleaseMapper;
import com.raccoon.release.dto.ReleaseScrapeResponse;
import com.raccoon.scraper.ReleaseScraper;
import com.raccoon.scraper.musicbrainz.MusicbrainzScraper;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReleaseScrapeServiceTest {

    ReleaseScrapeService service;

    @Mock
    UserArtistRepository userArtistRepositoryMock;
    @Mock
    ScrapeRepository scrapeRepositoryMock;
    @Mock
    Instance<ReleaseScraper<?>> mockScrapers;
    @Mock
    MusicbrainzScraper mockMusicbrainzScraper;
    @Mock
    SpotifyScraper mockSpotifyScraper;
    @Mock
    ReleaseMapper mockReleaseMapper;

    @Captor
    ArgumentCaptor<List<Long>> captor;

    @BeforeEach
    void setUp() {
        when(mockScrapers.stream()).thenReturn(Stream.of(mockMusicbrainzScraper, mockSpotifyScraper));

        service = new ReleaseScrapeService(
                mockScrapers,
                userArtistRepositoryMock,
                scrapeRepositoryMock,
                mockReleaseMapper
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
    void scrapeReleases_should_returnExistingScrape_when_lessThan1DayElapsed() throws ExecutionException, InterruptedException {
        var recentScrape = new Scrape();
        when(scrapeRepositoryMock.getMostRecentScrapeFrom(any())).thenReturn(Optional.of(recentScrape));

        service.scrapeReleases();

        verifyNoInteractions(mockMusicbrainzScraper, mockSpotifyScraper);
    }

    @Test
    void scrapeReleases_should_callBothScrapers() throws Exception {
        var releaseCount = 5;
        when(mockMusicbrainzScraper.scrapeReleases(any())).thenReturn(stubReleases(releaseCount));
        when(mockSpotifyScraper.scrapeReleases(any())).thenReturn(stubReleases(releaseCount));
        when(scrapeRepositoryMock.getMostRecentScrapeFrom(any())).thenReturn(Optional.empty());

        service.scrapeReleases();

        verify(mockMusicbrainzScraper, times(1)).scrapeReleases(Optional.empty());
        verify(mockSpotifyScraper, times(1)).scrapeReleases(Optional.empty());
    }

    @Test
    void updateHasNewRelease_withAlbums() {
        var releaseCount = 5;

        service.updateHasNewRelease(stubReleases(releaseCount));

        verify(userArtistRepositoryMock, times(1)).markNewRelease(captor.capture());
        final var arg = captor.getValue();
        assertThat(releaseCount)
                .isEqualTo(arg.size());
        // ids are given through `stubReleases` are lte `releaseCount`
        arg.forEach(artistId -> assertTrue(artistId < releaseCount));
    }

    @Test
    @DisplayName("Identical Releases returned by both scrapers, should be merged")
    void scrapeReleases_should_mergeIdenticalAlbums() throws Exception {
        var releaseCount = 5;
        Set<Release> stubReleases = stubReleases(releaseCount);
        when(scrapeRepositoryMock.getMostRecentScrapeFrom(any())).thenReturn(Optional.empty());
        when(mockMusicbrainzScraper.scrapeReleases(any())).thenReturn(stubReleases);
        // Second scraper returns only one Release already returned by the first
        when(mockSpotifyScraper.scrapeReleases(any())).thenReturn(Set.of(stubReleases.iterator().next()));

        ReleaseScrapeResponse scrape = service.scrapeReleases();

        assertThat(scrape.scrape().getReleaseCount())
                .isEqualTo(stubReleases.size());
    }

}
