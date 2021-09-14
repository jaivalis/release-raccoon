package com.raccoon.taste.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.scraper.lastfm.LastfmScraper;

import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    @DisplayName("If scrape took place not to long ago, should do nothing")
    void scrapeAfterJustScraped() {
        Set<UserArtist> artists = Set.of(
                new UserArtist()
        );
        user.setLastfmUsername("username");
        user.setLastLastFmScrape(LocalDateTime.now());
        user.setArtists(artists);

        service.updateTaste(user);

        assertEquals(artists, user.getArtists());
    }

    @Test
    @DisplayName("Scrape should update user artists")
    void testScrape() {
        user.setLastfmUsername("username");
        user.setLastLastFmScrape(LocalDateTime.now().minusDays(20));

        Artist stubArtist = new Artist();
        stubArtist.setName("stub artist");
        Collection<MutablePair<Artist, Float>> stubTaste = List.of(
                new MutablePair<>(stubArtist, 100F)
        );
        when(lastfmScraperMock.scrapeTaste(anyString(), any(Optional.class))).thenReturn(stubTaste);
        var userArtist = new UserArtist();
        userArtist.setArtist(stubArtist);
        userArtist.setUser(user);
        when(userArtistFactoryMock.getOrCreateUserArtist(user, stubArtist)).thenReturn(userArtist);

        service.updateTaste(user);

        assertEquals(1, user.getArtists().size());
        assertEquals(LocalDateTime.now().getDayOfMonth(), user.getLastLastFmScrape().getDayOfMonth());
        assertEquals(stubArtist, user.getArtists().iterator().next().getArtist());
        verify(userRepositoryMock, times(1)).persist(user);
    }

}