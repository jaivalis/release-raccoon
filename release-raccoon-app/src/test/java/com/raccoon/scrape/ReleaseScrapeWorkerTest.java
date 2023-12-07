package com.raccoon.scrape;

import com.raccoon.entity.Artist;
import com.raccoon.entity.ArtistRelease;
import com.raccoon.entity.Release;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.entity.repository.UserArtistRepository;
import com.raccoon.scrape.dto.ReleaseMapper;
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

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.enterprise.inject.Instance;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class ReleaseScrapeWorkerTest {

    ReleaseScrapeWorker worker;

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

        worker = new ReleaseScrapeWorker(
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
    void submit_should_callBothScrapers() throws Exception {
        var releaseCount = 5;
        when(mockMusicbrainzScraper.scrapeReleases(any())).thenReturn(stubReleases(releaseCount));
        when(mockSpotifyScraper.scrapeReleases(any())).thenReturn(stubReleases(releaseCount));

        worker.submitScrapeJobAsync();

        await("Should complete the scrape before we can query the latest scrape").atMost(Duration.ofSeconds(10))
                .until(() -> !worker.isRunning());
        verify(mockMusicbrainzScraper, times(1)).scrapeReleases(Optional.empty());
        verify(mockSpotifyScraper, times(1)).scrapeReleases(Optional.empty());
    }

    @Test
    @DisplayName("Identical Releases returned by both scrapers, should be merged")
    void submit_should_mergeIdenticalAlbums() throws Exception {
        var releaseCount = 5;
        Set<Release> stubReleases = stubReleases(releaseCount);
        when(mockMusicbrainzScraper.scrapeReleases(any())).thenReturn(stubReleases);
        // Second scraper returns only one Release already returned by the first
        when(mockSpotifyScraper.scrapeReleases(any())).thenReturn(Set.of(stubReleases.iterator().next()));

        worker.submitScrapeJobAsync();

        await("Should complete the scrape before we can query the latest scrape").atMost(Duration.ofSeconds(10))
                .until(() -> !worker.isRunning());
        assertThat(worker.getLatestScrape().getReleaseCount())
                .isEqualTo(stubReleases.size());
    }

    @Test
    void updateHasNewRelease_withAlbums() {
        var releaseCount = 5;

        worker.updateHasNewRelease(stubReleases(releaseCount));

        verify(userArtistRepositoryMock, times(1)).markNewRelease(captor.capture());
        final var arg = captor.getValue();
        assertThat(releaseCount)
                .isEqualTo(arg.size());
        // ids are given through `stubReleases` are lte `releaseCount`
        arg.forEach(artistId -> assertTrue(artistId < releaseCount));
    }

    @Test
    void onStop_ShouldShutdownExecutorService() {
        worker.onStop();

        assertThat(worker.executorService.isShutdown()).isTrue();
        assertThat(worker.executorService.isTerminated()).isTrue();
    }

}
