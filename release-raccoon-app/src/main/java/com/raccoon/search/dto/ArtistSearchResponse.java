package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistSearchResponse {

    @JsonProperty
    Collection<ArtistDto> fromDb;

    @JsonProperty
    Collection<ArtistDto> fromLastfm;

}