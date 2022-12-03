package com.raccoon.search.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResultArtistDtoTest {

    SearchResultArtistDto dto;

    @Test
    void merge() {
        dto = SearchResultArtistDto.builder()
                .lastfmUri(null)
                .musicbrainzId(null)
                .build();
        var other = SearchResultArtistDto.builder()
                .lastfmUri("some.uri")
                .musicbrainzId("some.id")
                .build();

        dto.merge(other);

        assertThat(dto.getMusicbrainzId())
                .isNotNull()
                .isEqualTo(other.getMusicbrainzId());
        assertThat(dto.getLastfmUri())
                .isNotNull()
                .isEqualTo(other.getLastfmUri());
    }

    @Test
    void mergeNullsShouldNotChange() {
        dto = SearchResultArtistDto.builder()
                .lastfmUri("some.uri")
                .musicbrainzId("some.id")
                .build();
        var other = SearchResultArtistDto.builder()
                .lastfmUri(null)
                .musicbrainzId(null)
                .build();

        dto.merge(other);

        assertThat(dto.getMusicbrainzId())
                .isNotNull();
        assertThat(dto.getLastfmUri())
                .isNotNull();
    }

}
