package com.raccoon.taste.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;
import com.raccoon.taste.TasteScrapeArtistWeightPairProcessor;

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
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyTasteUpdatingServiceTest {

    SpotifyTasteUpdatingService service;

    @Mock
    TasteScrapeArtistWeightPairProcessor mockTasteScrapeArtistWeightPairProcessor;
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
                mockTasteScrapeArtistWeightPairProcessor,
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
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setSpotifyEnabled(true);
        raccoonUser.setLastSpotifyScrape(LocalDateTime.now());
        when(mockUserRepository.findByIdOptional(0L)).thenReturn(Optional.of(raccoonUser));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    void testScrapeTasteShouldRedirect() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setSpotifyEnabled(true);
        raccoonUser.setLastSpotifyScrape(LocalDateTime.MIN);
        when(mockUserRepository.findByIdOptional(0L)).thenReturn(Optional.of(raccoonUser));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, response.getStatus());
    }

    @Test
    @DisplayName("RaccoonUser NotFoundException")
    void testScrapeNotFound() {
        when(mockUserRepository.findByIdOptional(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.updateTaste(100L));
    }

    @Test
    @DisplayName("Scrape should update raccoonUser artists")
    void testScrape() {
        RaccoonUser raccoonUser = new RaccoonUser();
        raccoonUser.setLastfmUsername("username");
        raccoonUser.setSpotifyEnabled(true);
        raccoonUser.setLastSpotifyScrape(LocalDateTime.MIN);

        Artist artist = new Artist();
        artist.setName("stub artist");
        Collection<MutablePair<Artist, Float>> stubTaste = List.of(
                new MutablePair<>(artist, 100F)
        );
        when(mockSpotifyScraper.fetchTopArtists(any(SpotifyUserAuthorizer.class))).thenReturn(stubTaste);
        var userArtist = new UserArtist();
        userArtist.setArtist(artist);
        userArtist.setUser(raccoonUser);
        when(mockUserRepository.findByIdOptional(any())).thenReturn(Optional.of(raccoonUser));
        when(mockTasteScrapeArtistWeightPairProcessor.delegateProcessArtistWeightPair(eq(raccoonUser), eq(artist), anyFloat(), any()))
                .thenReturn(userArtist);

        service.updateTaste(raccoonUser.id);

        assertEquals(1, raccoonUser.getArtists().size());
        assertEquals(LocalDateTime.now().getDayOfMonth(), raccoonUser.getLastSpotifyScrape().getDayOfMonth());
        assertEquals(artist, raccoonUser.getArtists().iterator().next().getArtist());
        verify(mockUserRepository, times(1)).persist(raccoonUser);
        verify(mockNotifyService, times(1)).notifySingleUser(eq(raccoonUser), any());
    }

}