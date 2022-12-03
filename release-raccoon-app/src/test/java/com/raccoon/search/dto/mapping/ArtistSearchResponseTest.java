package com.raccoon.search.dto.mapping;

import com.raccoon.search.dto.SearchResultArtistDto;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtistSearchResponseTest {

    ArtistSearchResponse response = ArtistSearchResponse.builder().build();

    @Test
    void setArtistsEmpty() {
        response.setArtists(Collections.emptyList());

        assertEquals(0, response.getCount());
    }

    @Test
    void setArtistsNonEmptyList() {
        response.setArtists(List.of(SearchResultArtistDto.builder().build()));

        assertEquals(1, response.getCount());
    }

}