package com.raccoon.release;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class ReleaseScrapeSchedulerTest {

    ReleaseScrapeScheduler scheduler;

    @Mock
    ReleaseScrapeService mockService;

    @BeforeEach
    void setup() {
        openMocks(this);

        scheduler = new ReleaseScrapeScheduler(mockService);
    }

    @Test
    @DisplayName("releaseScrapeCronJob() calls mockService.scrape()")
    void releaseScrapeCronJob() throws Exception {
        ReleaseScrapeScheduler serviceMock = mock(ReleaseScrapeScheduler.class);

        scheduler.releaseScrapeCronJob();

        verify(mockService, times(1)).scrape();
    }

}