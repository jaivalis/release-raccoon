package com.raccoon.scraper.lastfm;

import com.raccoon.scraper.config.LastFmConfig;

import de.umass.lastfm.Artist;
import de.umass.lastfm.Period;
import de.umass.lastfm.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class RaccoonLastfmApiTest {

    RaccoonLastfmApi api;

    @Mock
    LastFmConfig config;

    @BeforeEach
    void setUp() {
        openMocks(this);

        when(config.apiKey()).thenReturn("key");
        api = new RaccoonLastfmApi(config);
    }

    @ParameterizedTest
    @EnumSource(Period.class)
    void getUserTopArtists(Period period) {
        try (MockedStatic<User> mocked = mockStatic(User.class)) {
            var username = "username";
            mocked.when(() -> User.getTopArtists(anyString(), any(Period.class), anyString()))
                    .thenReturn(null);

            api.getUserTopArtists(username, period);

            mocked.verify(() -> User.getTopArtists(username, period, "key"));
        }
    }

    @Test
    void searchArtist() {
        try (MockedStatic<Artist> mocked = mockStatic(Artist.class)) {
            var artistName = "an Artist";
            mocked.when(() -> Artist.search(anyString(), anyString()))
                    .thenReturn(null);

            api.searchArtist(artistName);

            mocked.verify(() -> Artist.search(artistName, "key"));
        }
    }

}
