package com.raccoon.taste.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.NotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyTasteUpdatingServiceTest {

    SpotifyTasteUpdatingService service;

    @Mock
    UserArtistFactory mockUserArtistFactory;
    @Mock
    UserRepository mockUserRepository;
    @Mock
    SpotifyUserAuthorizer mockSpotifyUserAuthorizer;
    @Mock
    SpotifyScraper mockSpotifyScraper;
    @Mock
    NotifyService mockNotifyService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new SpotifyTasteUpdatingService(
                mockUserArtistFactory,
                mockUserRepository,
                mockSpotifyUserAuthorizer,
                mockSpotifyScraper,
                mockNotifyService
        );
    }

    @Test
    void testScrapeTasteUserNotFound() {
        when(mockUserRepository.findByIdOptional(0L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.scrapeTaste(0L));
    }

    @Test
    void testScrapeTasteRecentlyScraped() {
        User user = new User();
        user.setSpotifyEnabled(true);
        user.setLastSpotifyScrape(LocalDateTime.now());
        when(mockUserRepository.findByIdOptional(0L)).thenReturn(Optional.of(user));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    void testScrapeTasteShouldRedirect() {
        User user = new User();
        user.setSpotifyEnabled(true);
        user.setLastSpotifyScrape(LocalDateTime.MIN);
        when(mockUserRepository.findByIdOptional(0L)).thenReturn(Optional.of(user));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, response.getStatus());
    }

    @Test
    @DisplayName("NotFoundException")
    void testScrapeNotFound() {
        when(mockUserRepository.findByIdOptional(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateTaste(100L));
    }

    @Test
    @DisplayName("Scrape should update user artists")
    void testScrape() {
        User user = new User();
        user.setLastfmUsername("username");
        user.setSpotifyEnabled(true);
        user.setLastSpotifyScrape(LocalDateTime.MIN);

        Artist stubArtist = new Artist();
        stubArtist.setName("stub artist");
        Collection<MutablePair<Artist, Float>> stubTaste = List.of(
                new MutablePair<>(stubArtist, 100F)
        );
        when(mockSpotifyScraper.fetchTopArtists(any(SpotifyUserAuthorizer.class))).thenReturn(stubTaste);
        var userArtist = new UserArtist();
        userArtist.setArtist(stubArtist);
        userArtist.setUser(user);
        when(mockUserArtistFactory.getOrCreateUserArtist(user, stubArtist)).thenReturn(userArtist);
        when(mockUserRepository.findByIdOptional(any())).thenReturn(Optional.of(user));

        service.updateTaste(user.id);

        assertEquals(1, user.getArtists().size());
        assertEquals(LocalDateTime.now().getDayOfMonth(), user.getLastSpotifyScrape().getDayOfMonth());
        assertEquals(stubArtist, user.getArtists().iterator().next().getArtist());
        verify(mockUserRepository, times(1)).persist(user);
        verify(mockNotifyService, times(1)).notifySingleUser(eq(user), any());
    }

}