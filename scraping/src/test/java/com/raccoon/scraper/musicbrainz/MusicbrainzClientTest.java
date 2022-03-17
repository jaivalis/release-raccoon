package com.raccoon.scraper.musicbrainz;

import com.raccoon.scraper.config.MusicbrainzConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicbrainzClientTest {

    MusicbrainzClient client;

    @Mock
    MusicbrainzService mockMusicbrainzService;
    @Mock
    MusicbrainzConfig musicbrainzConfig;

    @BeforeEach
    void setUp() {
        when(musicbrainzConfig.queriesPerSecond()).thenReturn(5.0);

        client = new MusicbrainzClient(mockMusicbrainzService, musicbrainzConfig);
    }

    @Test
    @DisplayName("searchReleasesByDate(): Query should be formatted for Lucene")
    void searchReleasesByDate() {
        var date = LocalDate.of(2022, 1, 15);

        client.searchReleasesByDate(date, 200);

        verify(mockMusicbrainzService, times(1)).getReleasesByQuery("date:(2022\\-01\\-15)", "json", "100", "200");
    }

    @Test
    @DisplayName("searchArtistsByName(): Query should be formatted for Lucene")
    void searchArtistsByName() {
        var name = "artist";

        client.searchArtistsByName(name, 20, 30);

        verify(mockMusicbrainzService, times(1)).getArtistsByQuery(name, "json", "20", "30");
    }


    @Test
    @DisplayName("formatDateQuery(): Query should be formatted for Lucene: date:(YYYY\\-MM\\-dd)")
    void formatDateQuery() {
        var date = LocalDate.of(2022, 1, 15);

        String query = client.formatDateQuery(date);

        assertThat(query)
                .as("Date should follow the pattern: (`date:(yyyy\\-MM\\-dd`)")
                .isEqualTo("date:(2022\\-01\\-15)");
    }

    @Test
    @DisplayName("formatNameQuery(): Query should be formatted for Lucene: artist+name")
    void formatQuery() {
        var name = "artist name";

        String query = client.formatNameQuery(name);

        assertThat(query)
                .as("Name should follow the pattern: `artist+name`")
                .isEqualTo("artist+name");
    }

    @Test
    @DisplayName("formatNameQuery(): Name contains no spaces")
    void formatQueryNoSpaces() {
        var name = "artistname";

        String query = client.formatNameQuery(name);

        assertThat(query)
                .as("Name should follow the pattern: `artistname`")
                .isEqualTo("artistname");
    }

}
