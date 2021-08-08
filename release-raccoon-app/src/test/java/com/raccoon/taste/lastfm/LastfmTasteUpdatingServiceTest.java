package com.raccoon.taste.lastfm;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LastfmTasteUpdatingServiceTest {

    LastfmTasteUpdatingService service = new LastfmTasteUpdatingService();

    User user = new User();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("No lastFm username should do nothing")
    void scrapeNoLastFmUsername() {
        user.setLastfmUsername("");
        user.setArtists(Collections.emptySet());
        service.updateTaste(user);

        assertEquals(Collections.emptySet(), user.getArtists());
    }

    @Test
    @DisplayName("Just scraped should do nothing")
    void scrapeJustScraped() {
        Set<UserArtist> artists = Set.of(
                new UserArtist()
        );
        user.setLastfmUsername("username");
        user.setLastLastFmScrape(LocalDateTime.now());
        user.setArtists(artists);
        service.updateTaste(user);

        assertEquals(artists, user.getArtists());
    }

}