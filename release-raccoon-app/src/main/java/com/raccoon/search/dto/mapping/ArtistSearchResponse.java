package com.raccoon.search.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raccoon.search.dto.ArtistDto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistSearchResponse {

    /**
     * Results sorted by relevance
     */
    @JsonProperty
    List<ArtistDto> artists;

}