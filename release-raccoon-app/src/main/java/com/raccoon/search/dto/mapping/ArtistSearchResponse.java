package com.raccoon.search.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raccoon.search.dto.ArtistDto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistSearchResponse {

    private int count;

    /**
     * Results sorted by relevance
     */
    @JsonProperty
    private List<ArtistDto> artists;

    public void setArtists(List<ArtistDto> artists) {
        this.artists = artists;
        this.count = artists.size();
    }

}