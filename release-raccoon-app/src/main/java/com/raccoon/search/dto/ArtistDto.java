package com.raccoon.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ArtistDto {

    private final String id;
    private final String name;
    private final String lastfmUri;
    private final String spotifyUri;

}
