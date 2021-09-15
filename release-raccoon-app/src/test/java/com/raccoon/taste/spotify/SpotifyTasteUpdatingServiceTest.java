package com.raccoon.taste.spotify;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.scraper.spotify.SpotifyScraper;
import com.raccoon.scraper.spotify.SpotifyUserAuthorizer;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyTasteUpdatingServiceTest {

    SpotifyTasteUpdatingService service;

    @Mock
    UserArtistFactory userArtistFactoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @Mock
    SpotifyUserAuthorizer spotifyUserAuthorizerMock;
    @Mock
    SpotifyScraper spotifyScraperMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new SpotifyTasteUpdatingService(userArtistFactoryMock, userRepositoryMock, spotifyUserAuthorizerMock, spotifyScraperMock);
    }

    @Test
    void testScrapeTasteUserNotFound() {
        when(userRepositoryMock.findByIdOptional(0L)).thenReturn(Optional.empty());
        Assertions.assertThrows(NotFoundException.class, () -> service.scrapeTaste(0L));
    }

    @Test
    void testScrapeTasteSpotifyDisabled() {
        User user = new User();
        user.setSpotifyEnabled(false);
        when(userRepositoryMock.findByIdOptional(0L)).thenReturn(Optional.of(user));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    void testScrapeTasteRecentlyScraped() {
        User user = new User();
        user.setSpotifyEnabled(true);
        user.setLastSpotifyScrape(LocalDateTime.now());
        when(userRepositoryMock.findByIdOptional(0L)).thenReturn(Optional.of(user));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatus());
    }

    @Test
    void testScrapeTasteShouldRedirect() {
        User user = new User();
        user.setSpotifyEnabled(true);
        user.setLastSpotifyScrape(LocalDateTime.MIN);
        when(userRepositoryMock.findByIdOptional(0L)).thenReturn(Optional.of(user));

        final var response = service.scrapeTaste(0L);

        assertEquals(HttpStatus.SC_TEMPORARY_REDIRECT, response.getStatus());
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
        when(spotifyScraperMock.fetchTopArtists(any(SpotifyUserAuthorizer.class))).thenReturn(stubTaste);
        var userArtist = new UserArtist();
        userArtist.setArtist(stubArtist);
        userArtist.setUser(user);
        when(userArtistFactoryMock.getOrCreateUserArtist(user, stubArtist)).thenReturn(userArtist);

        service.updateTaste(user);

        assertEquals(1, user.getArtists().size());
        assertEquals(LocalDateTime.now().getDayOfMonth(), user.getLastSpotifyScrape().getDayOfMonth());
        assertEquals(stubArtist, user.getArtists().iterator().next().getArtist());
        verify(userRepositoryMock, times(1)).persist(user);
    }

}