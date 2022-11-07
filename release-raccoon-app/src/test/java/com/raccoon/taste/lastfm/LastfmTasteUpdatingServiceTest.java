package com.raccoon.taste.lastfm;

import com.raccoon.entity.Artist;
import com.raccoon.entity.RaccoonUser;
import com.raccoon.entity.UserArtist;
import com.raccoon.entity.factory.UserArtistFactory;
import com.raccoon.entity.repository.UserRepository;
import com.raccoon.notify.NotifyService;
import com.raccoon.scraper.lastfm.LastfmScraper;
import com.raccoon.taste.TasteScrapeArtistWeightPairProcessor;

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
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmTasteUpdatingServiceTest {

    LastfmTasteUpdatingService service;

    @Mock
    TasteScrapeArtistWeightPairProcessor mockTasteScrapeArtistWeightPairProcessor;
    @Mock
    UserArtistFactory userArtistFactoryMock;
    @Mock
    UserRepository userRepositoryMock;
    @Mock
    LastfmScraper lastfmScraperMock;
    @Mock
    NotifyService mockNotifyService;

    RaccoonUser raccoonUser = new RaccoonUser();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        service = new LastfmTasteUpdatingService(
                mockTasteScrapeArtistWeightPairProcessor,
                userRepositoryMock,
                lastfmScraperMock,
                mockNotifyService
        );
    }

    @Test
    @DisplayName("No lastFm username should do nothing")
    void scrapeNoLastFmUsername() {
        raccoonUser.setLastfmUsername("");
        raccoonUser.setArtists(Collections.emptySet());
        raccoonUser.id = 1L;
        when(userRepositoryMock.findById(raccoonUser.id)).thenReturn(raccoonUser);

        service.updateTaste(raccoonUser.id);

        assertEquals(Collections.emptySet(), raccoonUser.getArtists());
    }

    @Test
    @DisplayName("If scrape took place not to long ago, should do nothing")
    void scrapeAfterJustScraped() {
        Set<UserArtist> artists = Set.of(new UserArtist());
        raccoonUser.setLastfmUsername("username");
        raccoonUser.setLastLastFmScrape(LocalDateTime.now());
        raccoonUser.setArtists(artists);
        raccoonUser.id = 1L;
        when(userRepositoryMock.findById(raccoonUser.id)).thenReturn(raccoonUser);

        service.updateTaste(raccoonUser.id);

        assertEquals(artists, raccoonUser.getArtists());
        verify(mockNotifyService, never()).notifySingleUser(eq(raccoonUser), any());
    }

    @Test
    @DisplayName("Scrape should update raccoonUser artists and notify of release")
    void testScrape() {
        raccoonUser.setLastfmUsername("username");
        raccoonUser.setLastLastFmScrape(LocalDateTime.now().minusDays(20));

        Artist stubArtist = new Artist();
        stubArtist.setName("stub artist");
        Collection<MutablePair<Artist, Float>> stubTaste = List.of(
                new MutablePair<>(stubArtist, 100F)
        );
        when(lastfmScraperMock.scrapeTaste(anyString(), any(Optional.class))).thenReturn(stubTaste);
        var userArtist = new UserArtist();
        userArtist.setArtist(stubArtist);
        userArtist.setUser(raccoonUser);
        raccoonUser.id = 1L;
        when(userRepositoryMock.findById(raccoonUser.id)).thenReturn(raccoonUser);
        when(mockTasteScrapeArtistWeightPairProcessor.delegateProcessArtistWeightPair(eq(raccoonUser), eq(stubArtist), anyFloat(), any()))
                .thenReturn(userArtist);

        service.updateTaste(raccoonUser.id);

        assertEquals(1, raccoonUser.getArtists().size());
        assertEquals(LocalDateTime.now().getDayOfMonth(), raccoonUser.getLastLastFmScrape().getDayOfMonth());
        assertEquals(stubArtist, raccoonUser.getArtists().iterator().next().getArtist());
        verify(userRepositoryMock, times(1)).persist(raccoonUser);
        verify(mockNotifyService, times(1)).notifySingleUser(eq(raccoonUser), any());
    }

}