package com.raccoon.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaccoonUserTest {

    @Test
    @DisplayName("Should scrape lastfm if last scrape not set")
    void isLastfmScrapeNullRequired() {
        RaccoonUser raccoonUser = new RaccoonUser();
        assertTrue(raccoonUser.isLastfmScrapeRequired(1));
    }

    @Test
    @DisplayName("Should scrape lastfm if scraped long ago")
    void isLastfmScrapeRequired() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setLastLastFmScrape(LocalDateTime.now().minusDays(2));
        assertTrue(raccoonUser.isLastfmScrapeRequired(1));
    }

    @Test
    @DisplayName("Should not scrape lastfm if scraped recently")
    void isLastfmScrapeNotRequired() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setLastLastFmScrape(LocalDateTime.now());
        assertFalse(raccoonUser.isLastfmScrapeRequired(1));
    }

    @Test
    @DisplayName("Should scrape spotify if last scrape not set")
    void isSpotifyScrapeNullRequired() {
        RaccoonUser raccoonUser = new RaccoonUser();
        assertTrue(raccoonUser.isSpotifyScrapeRequired(1));
    }

    @Test
    @DisplayName("Should scrape spotify if scraped long ago")
    void isSpotifyScrapeRequired() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setLastSpotifyScrape(LocalDateTime.now().minusDays(2));
        assertTrue(raccoonUser.isSpotifyScrapeRequired(1));
    }

    @Test
    @DisplayName("Should not scrape spotify if scraped recently")
    void isSpotifyScrapeNotRequired() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setLastSpotifyScrape(LocalDateTime.now());
        assertFalse(raccoonUser.isSpotifyScrapeRequired(1));
    }
}