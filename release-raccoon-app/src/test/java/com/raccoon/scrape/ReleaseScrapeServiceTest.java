package com.raccoon.scrape;

import com.raccoon.entity.Scrape;
import com.raccoon.entity.repository.ScrapeRepository;
import com.raccoon.scraper.musicbrainz.MusicbrainzScraper;
import com.raccoon.scraper.spotify.SpotifyScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    ScrapeRepository scrapeRepositoryMock;
    @Mock
    ReleaseScrapeWorker mockWorker;
    @Mock
    MusicbrainzScraper mockMusicbrainzScraper;
    @Mock
    SpotifyScraper mockSpotifyScraper;

    @BeforeEach
    void setUp() {
        service = new ReleaseScrapeService(scrapeRepositoryMock, mockWorker);
    }

    @Test
    void scrapeReleases_should_returnExistingScrape_when_recentScrapeFound() {
        var recentScrape = new Scrape();
        when(scrapeRepositoryMock.getMostRecentScrapeFrom(any())).thenReturn(Optional.of(recentScrape));

        service.scrapeReleases();

        verifyNoInteractions(mockMusicbrainzScraper, mockSpotifyScraper);
    }

    @Test
    void scrapeReleases_should_submitWorkerTask_when_noRecentScrapeFound() {
        when(scrapeRepositoryMock.getMostRecentScrapeFrom(any())).thenReturn(Optional.empty());

        service.scrapeReleases();

        verify(mockWorker).submit();
    }

    @Test
    void scrapeReleases_should_notSubmitWorkerTask_when_noRecentScrapeFound_and_workerIsRunning() {
        when(scrapeRepositoryMock.getMostRecentScrapeFrom(any())).thenReturn(Optional.empty());
        when(mockWorker.isRunning()).thenReturn(true);

        service.scrapeReleases();

        verify(mockWorker, times(1)).isRunning();
        verify(mockWorker, times(0)).submit();
    }
}
