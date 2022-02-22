package com.raccoon.scraper.musicbrainz;

import com.flextrade.jfixture.JFixture;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MusicbrainzClientTest {

    MusicbrainzClient client;

    @Mock
    MusicbrainzService mockMusicbrainzService;

    JFixture fixture = new JFixture();
    MusicbrainzReleasesResponse musicbrainzReleasesResponse = fixture.create(MusicbrainzReleasesResponse.class);

    @BeforeEach
    void setUp() {
        client = new MusicbrainzClient(mockMusicbrainzService);
    }

    @Test
    @DisplayName("Query should be formatted for Lucene")
    void getForDate() {
        when(mockMusicbrainzService.getReleasesByQuery(any(), any(), any(), any())).thenReturn(musicbrainzReleasesResponse);
        var date = LocalDate.of(2022, 1, 15);

        client.getForDate(date, 200);

        verify(mockMusicbrainzService, times(1)).getReleasesByQuery("date:(2022\\-01\\-15)", "json", "100", "200");
    }

    @Test
    @DisplayName("Query should be formatted for Lucene: date%3A%28yyyy%5C-MM%5C-dd%29")
    void formatQuery() {
        var date = LocalDate.of(2022, 1, 15);

        String encoded = client.formatQuery(date);

        assertThat(encoded)
                .as("Date encoding should follow the pattern: (`date:(yyyy\\-MM\\-dd`)")
                .isEqualTo("date:(2022\\-01\\-15)");
    }

}
