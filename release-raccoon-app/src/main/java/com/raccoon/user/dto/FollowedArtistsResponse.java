package com.raccoon.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.raccoon.dto.ArtistDto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowedArtistsResponse {

    @JsonProperty
    private int total;

    @JsonProperty
    private List<ArtistDto> rows;

}
