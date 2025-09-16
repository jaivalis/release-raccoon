package com.raccoon.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArtistDtoTest {

    ArtistDto dto;

    @Test
    void getLastfmUri_should_returnFormattedUri_when_lastfmUriIsNull() {
        dto = ArtistDto.builder()
                .name("John Doe")
                .lastfmUri(null)
                .build();

        assertThat(dto.getLastfmUri())
                .isNotNull()
                .isEqualTo("https://www.last.fm/music/John+Doe");
    }

    @Test
    void getLastfmUri_should_returnLastfmUri_when_lastfmUriIsNotEmpty() {
        dto = ArtistDto.builder()
                .name("John Doe")
                .lastfmUri("https://custom.uri")
                .build();

        assertThat(dto.getLastfmUri())
                .isNotNull()
                .isEqualTo("https://custom.uri");
    }
}