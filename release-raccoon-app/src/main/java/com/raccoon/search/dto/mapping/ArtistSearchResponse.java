package com.raccoon.search.dto.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raccoon.search.dto.ArtistDto;

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