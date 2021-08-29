package com.raccoon.taste.lastfm;

import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.scraper.LastfmScraper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmTasteUpdatingServiceTest {

    LastfmTasteUpdatingService service;

    @Mock
    UserArtistFactory userArtistFactoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @Mock
    LastfmScraper lastfmScraperMock;

    User user = new User();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new LastfmTasteUpdatingService(userArtistFactoryMock, userRepositoryMock, lastfmScraperMock);
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
        when(userRepositoryMock.isLastfmScrapeRequired(anyInt(), any(LocalDateTime.class))).thenReturn(false);

        service.updateTaste(user);

        assertEquals(artists, user.getArtists());
    }

}