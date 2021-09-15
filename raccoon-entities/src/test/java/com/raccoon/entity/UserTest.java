package com.raccoon.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    @DisplayName("Should scrape lastfm if last scrape not set")
    void isLastfmScrapeNullRequired() {
        User user = new User();
        assertTrue(user.isLastfmScrapeRequired(1));
    }

    @Test
    @DisplayName("Should scrape lastfm if scraped long ago")
    void isLastfmScrapeRequired() {
        User user = new User();
        user.setLastLastFmScrape(LocalDateTime.now().minusDays(2));
        assertTrue(user.isLastfmScrapeRequired(1));
    }

    @Test
    @DisplayName("Should not scrape lastfm if scraped recently")
    void isLastfmScrapeNotRequired() {
        User user = new User();
        user.setLastLastFmScrape(LocalDateTime.now());
        assertFalse(user.isLastfmScrapeRequired(1));
    }

    @Test
    @DisplayName("Should scrape spotify if last scrape not set")
    void isSpotifyScrapeNullRequired() {
        User user = new User();
        assertTrue(user.isSpotifyScrapeRequired(1));
    }

    @Test
    @DisplayName("Should scrape spotify if scraped long ago")
    void isSpotifyScrapeRequired() {
        User user = new User();
        user.setLastSpotifyScrape(LocalDateTime.now().minusDays(2));
        assertTrue(user.isSpotifyScrapeRequired(1));
    }

    @Test
    @DisplayName("Should not scrape spotify if scraped recently")
    void isSpotifyScrapeNotRequired() {
        User user = new User();
        user.setLastSpotifyScrape(LocalDateTime.now());
        assertFalse(user.isSpotifyScrapeRequired(1));
    }
}