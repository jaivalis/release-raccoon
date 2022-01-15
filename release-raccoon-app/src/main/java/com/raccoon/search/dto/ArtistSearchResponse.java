package com.raccoon.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ArtistSearchResponse {

    @JsonProperty
    Map<String, Collection<ArtistDto>> artistsPerResource;

}