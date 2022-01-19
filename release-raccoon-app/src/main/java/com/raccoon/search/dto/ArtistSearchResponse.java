package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

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