package com.raccoon.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArtistDto {

    private final String id;
    private final String name;
    private final String lastfmUri;
    private final String spotifyUri;

}
