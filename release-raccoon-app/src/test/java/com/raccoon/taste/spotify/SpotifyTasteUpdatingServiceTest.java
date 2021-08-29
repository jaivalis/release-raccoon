package com.raccoon.taste.spotify;

import com.raccoon.entity.User;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyTasteUpdatingServiceTest {

    SpotifyTasteUpdatingService service;

    @Mock
    UserArtistFactory userArtistFactory;
    @Mock
    UserRepository userRepository;
    @Mock
    SpotifyUserAuthorizer spotifyUserAuthorizer;
    @Mock
    SpotifyScraper spotifyScraper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new SpotifyTasteUpdatingService(userArtistFactory, userRepository, spotifyUserAuthorizer, spotifyScraper);
    }

    @Test
    void testScrapeTasteUserNotFound() {
        when(userRepository.findByIdOptional(eq(0L))).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> service.scrapeTaste(0L));
    }

    @Test
    void testScrapeTasteSpotifyDisabled() {
        User u = new User();
        u.setSpotifyEnabled(false);
        when(userRepository.findByIdOptional(eq(0L))).thenReturn(Optional.of(u));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    void testScrapeTasteRecentlyScraped() {
        User u = new User();
        u.setSpotifyEnabled(true);
        u.setLastSpotifyScrape(LocalDateTime.now());
        when(userRepository.findByIdOptional(eq(0L))).thenReturn(Optional.of(u));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
    }

}