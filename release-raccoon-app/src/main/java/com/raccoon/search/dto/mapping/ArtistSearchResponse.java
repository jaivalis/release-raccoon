package com.raccoon.search.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raccoon.search.dto.SearchResultArtistDto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistSearchResponse {

    @JsonProperty
    private int count;

    /**
     * Results sorted by relevance
     */
    @JsonProperty
    private List<SearchResultArtistDto> artists;

    public void setArtists(List<SearchResultArtistDto> artists) {
        this.artists = artists;
        this.count = artists.size();
    }

}
